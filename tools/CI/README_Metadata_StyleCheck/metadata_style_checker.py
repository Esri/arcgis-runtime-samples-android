#!/usr/bin/env python3

import os
import re
import json
import typing
import argparse

from pathlib import Path

# region Global sets
# A set of category folder names in current sample viewer.
categories = {
    'Analysis',
    'Augmented Reality',
    'Cloud and Portal',
    'Layers',
    'Edit and Manage Data',
    'Maps and Scenes',
    'MapViews, SceneViews and UI',
    'Routing and Logistics',
    'Search and Query',
    'Utility Networks',
    'Visualization'
}

# A set of languages valid for samples in current sample viewer.
languages = {
    'java',
    'kotlin'
}

# endregion

# region Static functions
def sub_special_char(string: str) -> str:
    """
    Check and substitute if a string contains special characters.

    :param string: The input string.
    :return: A new string without special characters.
    """
    # regex = re.compile('[@_!#$%^&*()<>?/\\|}{~:]')
    regex = re.compile(r'[@_!#$%^&*<>?|/\\}{~:]')
    return re.sub(regex, '', string)


def parse_head(head_string: str) -> (str, str):
    """
    Parse the `Title` section of README file and get the title and description.

    :param head_string: A string containing title, description and images.
    :return: Stripped title and description strings.
    """
    parts = list(filter(bool, head_string.splitlines()))
    if len(parts) < 3:
        raise Exception('README description parse failure!')
    title = parts[0].lstrip('# ').rstrip()
    description = parts[1].strip()
    return title, description


def parse_apis(apis_string: str) -> typing.List[str]:
    """
    Parse the `Relevant API` section and get a list of APIs.

    :param apis_string: A string containing all APIs.
    :return: A sorted list of stripped API names.
    """
    apis = list(filter(bool, apis_string.splitlines()))
    if not apis:
        raise Exception('README Relevant API parse failure!')
    return sorted([api.lstrip('*- ').rstrip() for api in apis])


def parse_tags(tags_string: str) -> typing.List[str]:
    """
    Parse the `Tags` section and get a list of tags.

    :param tags_string: A string containing all tags, with comma as delimiter.
    :return: A sorted list of stripped tags.
    """
    tags = tags_string.split(',')
    if not tags:
        raise Exception('README Tags parse failure!')
    return sorted([tag.strip() for tag in tags])

def parse_offline_data(offline_data_string: str) -> typing.List[str]:

    # extract any guids - these are AGOL items
    regex = re.compile('[0-9a-f]{8}[0-9a-f]{4}[1-5][0-9a-f]{3}[89ab][0-9a-f]{3}[0-9a-f]{12}', re.I)
    matches = re.findall(regex, offline_data_string)

    return list(dict.fromkeys(matches))


def get_folder_name_from_path(path: str, index: int = -1) -> str:
    """
    Get the folder name from a full path.

    :param path: A string of a full/absolute path to a folder.
    :param index: The index of path parts. Default to -1 to get the most
    trailing folder in the path; set to certain index to get other parts.
    :return: The folder name.
    """
    return os.path.normpath(path).split(os.path.sep)[index]
# endregion


class MetadataCreator:

    def __init__(self, folder_path: str):
        """
        The standard format of metadata.json for Android platform. Read more at:
        /common-samples/wiki/README.metadata.json
        """
        self.category = ''          # Populate from path.
        self.description = ''       # Populate from README.
        self.ignore = False         # Default to False.
        self.images = []            # Populate from paths.
        self.keywords = []          # Populate from README.
        self.language = ''          # Populate from metadata.
        self.offline_data = False   # Default to False.
        self.redirect_from = []     # Default to empty list.
        self.relevant_apis = []     # Populate from README.
        self.snippets = []          # Populate from paths.
        self.title = ''             # Populate from README.

        self.folder_path = folder_path
        self.folder_name = get_folder_name_from_path(folder_path)
        self.readme_path = os.path.join(folder_path, 'README.md')
        self.json_path = os.path.join(folder_path, 'README.metadata.json')

    def get_source_code_paths(self) -> typing.List[str]:
        """
        Traverse the directory and get all filenames for source code.

        :return: A list of kotlin and java source code filenames.
        """
        results = []

        paths = Path(self.folder_path).glob('**/*.java')
        for path in paths:
            results.append(os.path.relpath(path, self.folder_path))

        paths = Path(self.folder_path).glob('**/*.kt')
        for path in paths:
            results.append(os.path.relpath(path, self.folder_path))

        if not results:
            raise Exception('Unable to get kotlin or java source code paths.')

        results = list(filter(lambda x: 'build/' not in x, results)) # exclude \build folder
        results = list(map(lambda x: x.replace(os.sep, '/'), results)) # eliminate double backslashes in the paths

        return sorted(results)

    def get_images_paths(self):
        """
        Traverse the directory and get all filenames for images.

        :return: A list of image filenames.
        """
        results = []
        for file in os.listdir(self.folder_path):
            if os.path.splitext(file)[1].lower() in ['.png']:
                results.append(file)
        if not results:
            raise Exception('Unable to get images paths.')
        return sorted(results)

    def populate_from_readme(self) -> None:
        """
        Read and parse the sections from README, and fill in the 'title',
        'description', 'relevant_apis' and 'keywords' fields in the dictionary
        for output json.
        """
        pathparts = splitall(self.readme_path)
        self.formal_name = pathparts[-2].replace("-", " ").title().replace(" ", "")

        try:
            readme_file = open(self.readme_path, 'r')
            # read the readme content into a string
            readme_contents = readme_file.read()
        except Exception as err:
            print(f"Error reading README - {self.readme_path} - {err}.")
            raise err
        else:
            readme_file.close()

        # Use regex to split the README by exactly 2 pound marks, so that they
        # are separated into paragraphs.
        pattern = re.compile(r'^#{2}(?!#)\s(.*)', re.MULTILINE)
        readme_parts = re.split(pattern, readme_contents)
        try:
            api_section_index = readme_parts.index('Relevant API') + 1
            tags_section_index = readme_parts.index('Tags') + 1
            self.title, self.description = parse_head(readme_parts[0])
            self.relevant_apis = parse_apis(readme_parts[api_section_index])
            keywords = parse_tags(readme_parts[tags_section_index])
            # De-duplicate API names in README's Tags section.
            self.keywords = [w for w in keywords if w not in self.relevant_apis]
            if readme_parts.__contains__('Offline data'):
                offline_data_section_index = readme_parts.index('Offline data') + 1
                self.offline_data = parse_offline_data(readme_parts[offline_data_section_index])

        except Exception as err:
            print(f'Error parsing README - {self.readme_path} - {err}.')
            raise err

    def populate_from_paths(self) -> None:
        """
        Populate source code and image filenames from a sample's
        folder.
        """
        try:
            self.images = self.get_images_paths()
            self.snippets = self.get_source_code_paths()
        except Exception as err:
            print(f"Error parsing paths - {self.folder_name} - {err}.")
            raise err

    def flush_to_json_string(self) -> str:
        """
        Write the metadata to a json string.
        """
        data = dict()

        data["category"] = self.category
        data["description"] = self.description
        data["formal_name"] = self.formal_name
        data["ignore"] = self.ignore
        data["images"] = self.images
        data["keywords"] = self.keywords
        if (self.offline_data != False):
            data["offline_data"] = self.offline_data
        data["language"] = self.language
        data["redirect_from"] = self.redirect_from
        data["relevant_apis"] = self.relevant_apis
        data["snippets"] = self.snippets
        data["title"] = self.title

        return json.dumps(data, indent=4, sort_keys=True)


def compare_one_metadata(folder_path: str):
    """
    A handy helper function to create 1 sample's metadata by running the script
    without passing in arguments, and write to a separate json for comparison.

    The path may look like
    '~/arcgis-runtime-samples-android/kotlin/add-enc-exchange-set'
    """
    single_updater = MetadataCreator(folder_path)
    try:
        single_updater.populate_from_readme()
        single_updater.populate_from_paths()
    except Exception as err:
        print(f'Error populate failed for - {single_updater.folder_name}.')
        raise err

    json_path = os.path.join(folder_path, 'README.metadata.json')

    try:
        json_file = open(json_path, 'r')
        json_data = json.load(json_file)
    except Exception as err:
        print(f'Error reading JSON - {folder_path} - {err}')
        raise err
    else:
        json_file.close()
    # The special rule not to compare the redirect_from.
    single_updater.redirect_from = json_data['redirect_from']
    # The special rule to check for valid category, but not compare them to anything since the
    # category is only specified in the metadata.
    if (json_data['category'] in categories):
        single_updater.category = json_data['category']
    else:
        single_updater.category = "INVALID CATEGORY"
    # The special rule to check for valid language, but not compare them to anything since the
    # language is only specified in the metadata.
    if (json_data['language'] in languages):
        single_updater.language = json_data['language']
    else:
        single_updater.language = "INVALID LANGUAGE"

    # The special rule to be lenient on shortened description.
    # If the original json has a shortened/special char purged description,
    # then no need to raise an error.
    if json_data['description'] in sub_special_char(single_updater.description):
        single_updater.description = json_data['description']
    # The special rule to ignore the order of src filenames.
    # If the original json has all the filenames, then it is good.
    if sorted(json_data['snippets']) == single_updater.snippets:
        single_updater.snippets = json_data['snippets']

    new = single_updater.flush_to_json_string()
    print(new)
    original = json.dumps(json_data, indent=4, sort_keys=True)
    print(original)
    if new != original:
        raise Exception(f'Error inconsistent metadata - {folder_path}')


def all_samples(path: str):
    """
    Run the check on all samples.

    :param path: The path to 'arcgis-runtime-samples-android' folder.
    :return: None. Throws if exception occurs.
    """
    exception_count = 0
    for root, dirs, files in os.walk(path):
        for dir_name in dirs:
            sample_path = os.path.join(root, dir_name)
            # Omit empty folders - they are omitted by Git.
            if len([f for f in os.listdir(sample_path)
                    if not f.startswith('.DS_Store')]) == 0:
                continue
            try:
                compare_one_metadata(sample_path)
            except Exception as err:
                exception_count += 1
                print(f'{exception_count}. {err}')

    # Throw once if there are exceptions.
    if exception_count > 0:
        raise Exception('Error(s) occurred during checking all samples.')

def splitall(path):
        ## Credits: taken verbatim from https://www.oreilly.com/library/view/python-cookbook/0596001673/ch04s16.html
        allparts = []
        while 1:
            parts = os.path.split(path)
            if parts[0] == path:  # sentinel for absolute paths
                allparts.insert(0, parts[0])
                break
            elif parts[1] == path: # sentinel for relative paths
                allparts.insert(0, parts[1])
                break
            else:
                path = parts[0]
                allparts.insert(0, parts[1])
        return allparts

def main():
    # Initialize parser.
    msg = 'Check metadata style. Run it against the samples repo root, or a single sample folder. ' \
          'On success: Script will exit with zero. ' \
          'On failure: Title inconsistency will print to console and the ' \
          'script will exit with non-zero code.'
    parser = argparse.ArgumentParser(description=msg)
    parser.add_argument('-a', '--all', help='path to the samples repo root')
    parser.add_argument('-s', '--single', help='path to a single sample')
    args = parser.parse_args()

    if args.single:
        try:
            compare_one_metadata(args.single)
        except Exception as err:
            raise err
    elif args.all:
        try:
            all_samples(args.all)
        except Exception as err:
            raise err
    else:
        raise Exception('Invalid arguments, abort.')


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        print(f'{error}')
        exit(1)
