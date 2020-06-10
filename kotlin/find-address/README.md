# Find address

Find the location for an address.

![Image of find address](find-address.png)

## Use case

A user can input a raw address into your app's search bar and zoom to the address location.

## How to use the sample

For simplicity, the sample comes loaded with a set of suggested addresses. Choose an address from the suggestions list above the search view, or type in an address in the search view. Suggestions will appear as text is entered. Tap a suggestion or submit your own address to show its location on the map in a callout.

## How it works
1. Create a `LocatorTask` using the URL to a locator service.
2. Set the `GeocodeParameters` for the locator task and specify the geocode's attributes.
3. Get the matching results from the `GeocodeResult` using `locatorTask.geocodeAsync(addressString, geocodeParameters)`.
4. Create a `Graphic` with the geocode result's location and store the geocode result's attributes in the graphic's attributes.
5. Show the graphic in a `GraphicsOverlay`.

## Relevant API
* GeocodeParameters
* GeocodeResult
* LocatorTask

## Tags

address, geocode, locator, search
