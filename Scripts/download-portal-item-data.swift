//
// Copyright © 2019 Esri.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// This scripts downloads data for portal items. It takes three arguments. The
// first is a path to a plist file defining the portal items to download. The
// is a path to a plist file defining how various file types should be
// organized. The third is a path to the download directory.
//
// A mapping of item identifiers to relative paths is maintained inside the
// download directory. This mapping affords efficiently checking whether an
// item has already been downloaded.

import Foundation

protocol URLProvider {
    func makeURL(filename: String) -> URL
}

struct DestinationURLProvider: URLProvider {
    let downloadDirectory: URL
    let fileTypes: [String: [String]]
    
    /// Make a full path for a file based on the mapping between its filename
    /// and file type.
    ///
    /// - Parameter filename: The filename of the file.
    /// - Returns: A URL to the file.
    func makeURL(filename: String) -> URL {
        var url = downloadDirectory
        if let subdirectory = fileTypes.first(where: {
            $0.value.contains((filename as NSString).pathExtension) })?.key {
            url.appendPathComponent(subdirectory, isDirectory: true)
        }
        url.appendPathComponent(filename, isDirectory: false)
        return url
    }
}

/// Creates a URL such as
/// `{portalURL}/sharing/rest/content/items/{itemIdentifier}/data`
/// for the given item in the given portal.
///
/// - Parameters:
///   - portalURL: The URL of the portal.
///   - itemIdentifier: The identifier of the item.
/// - Returns: A new URL.
func makeDataURL(portalURL: URL, itemIdentifier: String) -> URL {
    return portalURL
        .appendingPathComponent("sharing")
        .appendingPathComponent("rest")
        .appendingPathComponent("content")
        .appendingPathComponent("items")
        .appendingPathComponent(itemIdentifier)
        .appendingPathComponent("data")
}

/// Returns the name of the file in the ZIP archive at the given url.
///
/// - Parameter url: The url to a ZIP archive.
/// - Throws: Exceptions when running the `zipinfo` process.
/// - Returns: The file name.
func nameOfFileInArchive(at url: URL) throws -> String {
    let outputPipe = Pipe()
    let process = Process()
    process.executableURL = URL(fileURLWithPath: "/usr/bin/zipinfo", isDirectory: false)
    process.arguments = ["-1", url.path]
    process.standardOutput = outputPipe
    try process.run()
    process.waitUntilExit()
    
    let filenameData = outputPipe.fileHandleForReading.readDataToEndOfFile()
    return String(data: filenameData, encoding: .utf8)!.trimmingCharacters(in: .whitespacesAndNewlines)
}

/// Count files in an archive.
///
/// - Parameter url: The url to a ZIP archive.
/// - Throws: Exceptions when running the `zipinfo` process.
/// - Returns: The file count in the archive.
func numberOfFilesInArchive(at url: URL) throws -> Int {
    let outputPipe = Pipe()
    let process = Process()
    process.executableURL = URL(fileURLWithPath: "/usr/bin/zipinfo", isDirectory: false)
    process.arguments = ["-t", url.path]
    process.standardOutput = outputPipe
    try process.run()
    process.waitUntilExit()
    
    // The totals info looks like
    // "240 files, 29461066 bytes uncompressed, 28292775 bytes compressed:  4.0%"
    // To extract the count, cut the string when first space char is met.
    let totalsInfo = outputPipe.fileHandleForReading.readDataToEndOfFile()
    let totalsCount = String(data: totalsInfo.prefix { $0 != 32 }, encoding: .utf8)!
    return Int(totalsCount)!
}

/// Uncompresses the data in the archive at the source URL into the destination URL.
///
/// - Parameters:
///   - sourceURL: The URL of a ZIP archive.
///   - destinationURL: The URL at which to uncompress the archive.
func uncompressArchive(at sourceURL: URL, to destinationURL: URL) throws {
    let process = Process()
    process.executableURL = URL(fileURLWithPath: "/usr/bin/unzip", isDirectory: false)
    // Unzip the archive into a specified sub-folder and silence the output.
    // "-j" is passed in to get rid of redundant subfolder.
    process.arguments = ["-jq", sourceURL.path, "-d", destinationURL.path]
    
    try process.run()
    process.waitUntilExit()
}

/// Download file from portal and write the file(s) to appropriate path(s).
///
/// - Parameters:
///   - sourceURL: The portal URL to the resource.
///   - destinationURLProvider: A helper struct to make destination URL with filename.
///   - completion: A closure to handle the results.
func downloadFile(at sourceURL: URL, destinationURLProvider: URLProvider, completion: @escaping (Result<URL, Error>) -> Void) {
    let downloadTask = URLSession.shared.downloadTask(with: sourceURL) { (temporaryURL, response, error) in
        if let temporaryURL = temporaryURL, let response = response {
            do {
                let suggestedFilename = response.suggestedFilename!
                let downloadName: String
                let isArchive = (suggestedFilename as NSString).pathExtension == "zip"
                // If the downloaded file is an archive and contains
                //   - 1 file, use the name of that file.
                //   - multiple files, use the suggested filename (*.zip).
                // If it is not an archive, use the server suggested filename.
                if isArchive {
                    let count = try numberOfFilesInArchive(at: temporaryURL)
                    if count > 1 {
                        downloadName = suggestedFilename
                    } else {
                        downloadName = try nameOfFileInArchive(at: temporaryURL)
                    }
                } else {
                    downloadName = suggestedFilename
                }
                let downloadURL = destinationURLProvider.makeURL(filename: downloadName)
                
                try FileManager.default.createDirectory(
                    at: downloadURL.deletingLastPathComponent(),
                    withIntermediateDirectories: true
                )
                
                if FileManager.default.fileExists(atPath: downloadURL.path) {
                    try FileManager.default.removeItem(at: downloadURL)
                }
                
                if isArchive {
                    let extractURL = downloadURL.pathExtension == "zip"
                        // Uncompress to directory named after archive.
                        ? downloadURL.deletingPathExtension()
                        // Uncompress to appropriate subdirectory.
                        : downloadURL.deletingLastPathComponent()
                    try uncompressArchive(at: temporaryURL, to: extractURL)
                } else {
                    try FileManager.default.moveItem(at: temporaryURL, to: downloadURL)
                }
                
                completion(.success(downloadURL))
            } catch {
                completion(.failure(error))
            }
        } else if let error = error {
            completion(.failure(error))
        }
    }
    downloadTask.resume()
}

/// A type that describes an item in a portal.
struct PortalItem: Decodable {
    /// The identifier of the item.
    var identifier: String
    /// The filename of the item.
    var filename: String
}

// MARK: Script Entry

let arguments = CommandLine.arguments

guard arguments.count == 4 else {
    print("Invalid number of arguments")
    exit(1)
}

let portalItemsURL = URL(fileURLWithPath: arguments[1], isDirectory: false)
let fileTypesURL = URL(fileURLWithPath: arguments[2], isDirectory: false)
let downloadDirectoryURL = URL(fileURLWithPath: arguments[3], isDirectory: true)

if !FileManager.default.fileExists(atPath: downloadDirectoryURL.path) {
    do {
        try FileManager.default.createDirectory(at: downloadDirectoryURL, withIntermediateDirectories: false)
    } catch {
        print("Error creating download directory: \(error)")
        exit(1)
    }
}

let portalItems: [String: [PortalItem]] = {
    do {
        let data = try Data(contentsOf: portalItemsURL)
        return try PropertyListDecoder().decode([String: [PortalItem]].self, from: data)
    } catch {
        print("Error decoding portal items: \(error)")
        exit(1)
    }
}()

let fileTypes: [String: [String]] = {
    do {
        let data = try Data(contentsOf: fileTypesURL)
        return try PropertyListDecoder().decode([String: [String]].self, from: data)
    } catch {
        print("Error decoding file types: \(error)")
        exit(1)
    }
}()

let destinationURLProvider = DestinationURLProvider(
    downloadDirectory: downloadDirectoryURL,
    fileTypes: fileTypes
)

typealias Identifier = String
typealias Filename = String
typealias DownloadedItems = [Identifier: Filename]

let downloadedItemsURL = downloadDirectoryURL.appendingPathComponent(".downloaded_items", isDirectory: false)
let previousDownloadedItems: DownloadedItems = {
    do {
        let data = try Data(contentsOf: downloadedItemsURL)
        let decoder = PropertyListDecoder()
        return try decoder.decode(DownloadedItems.self, from: data)
    } catch {
        return [:]
    }
}()
var downloadedItems = previousDownloadedItems

let dispatchGroup = DispatchGroup()

portalItems.forEach { (portalURLString, portalItems) in
    let portalURL = URL(string: portalURLString)!
    portalItems.forEach { (portalItem) in
        // Have we already downloaded the item?
        let filename = downloadedItems[portalItem.identifier] ?? portalItem.filename
        let tempURL = destinationURLProvider.makeURL(filename: filename)
        let fileURL = tempURL.pathExtension == "zip" ? tempURL.deletingPathExtension() : tempURL
        
        // Check if a single file or a directory exists.
        if FileManager.default.fileExists(atPath: fileURL.path) {
            print("Item \(portalItem.identifier) has already been downloaded")
            // This is a temporary measure for users who currently don't have a downloaded items file.
            downloadedItems[portalItem.identifier] = filename
        } else {
            print("Downloading item \(portalItem.identifier)")
            fflush(stdout)
            
            dispatchGroup.enter()
            let sourceURL = makeDataURL(portalURL: portalURL, itemIdentifier: portalItem.identifier)
            downloadFile(at: sourceURL, destinationURLProvider: destinationURLProvider) { (result) in
                switch result {
                case .success(let url):
                    // ' + 1' removes the leading path separator.
                    downloadedItems[portalItem.identifier] = url.lastPathComponent
                    dispatchGroup.leave()
                case .failure(let error):
                    print("Warning: Error downloading item \(portalItem.identifier): \(error)")
                    URLSession.shared.invalidateAndCancel()
                    exit(1)
                }
            }
        }
    }
}

dispatchGroup.wait()

// Update the downloaded items file if needed.
if downloadedItems != previousDownloadedItems {
    do {
        let encoder = PropertyListEncoder()
        let data = try encoder.encode(downloadedItems)
        try data.write(to: downloadedItemsURL)
    } catch {
        print("Warning: Error recording downloaded items: \(error)")
        exit(1)
    }
}
