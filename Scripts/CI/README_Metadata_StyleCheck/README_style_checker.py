#!/usr/bin/env python3

"""
Comments in PyCharm style.

References

- Tag sorter by Zack
  /common-samples/blob/master/tools/net/tag_sorter/tag_sorter.py

- README standard format
  /common-samples/wiki/Standard-sample-documentation-template-%28README.md%29
"""

import os
import re
import typing
import argparse

# region Global sets
# A set of words that get omitted during letter-case checks.
exception_proper_nouns = {
    'WmsLayer',
    'ArcGIS Online',
    'OAuth',
    'Web Mercator',
    'ArcGIS Pro',
    'GeoPackage',
    'loadStatus',
    'Integrated Windows Authentication',
    'GeoElement',
    'Network Link',
    'Network Link Control',
    'Open Street Map',
    'OpenStreetMap',
    'Play a KML Tour'
}

# A set of category folder names in current sample viewer.
categories = {
    'Maps',
    'Layers',
    'Features',
    'Display information',
    'Search',
    'Edit data',
    'Geometry',
    'Route and directions',
    'Analysis',
    'Cloud and portal',
    'Scenes',
    'Utility network',
    'Augmented reality'
}
# endregion


# region Static functions
def get_folder_name_from_path(path: str, index: int = -1) -> str:
    """
    Get the folder name from a full path.

    :param path: A string of a full/absolute path to a folder.
    :param index: The index of path parts. Default to -1 to get the most
    trailing folder in the path; set to certain index to get other parts.
    :return: The folder name.
    """
    return os.path.normpath(path).split(os.path.sep)[index]


def parse_head(head_string: str) -> (str, str):
    """
    Parse the head of README and get title and description.

    :param head_string: A string containing title, description and images.
    :return: Stripped title and description strings.
    """
    # Split title section and rule out empty lines.
    parts = list(filter(bool, head_string.splitlines()))
    if len(parts) < 3:
        raise Exception('README should contain title, description and image.')
    title = parts[0].lstrip('# ').rstrip()
    description = parts[1].strip()
    return title, description


def check_apis(apis_string: str) -> typing.Set[str]:
    """
    Check the format for `Relevant API` section.

    :param apis_string: A multiline string containing all APIs.
    :return: A set of APIs. Throws if format is wrong.
    """
    stripped = apis_string.strip()
    apis = list(stripped.splitlines())
    if not apis:
        raise Exception('Empty Relevant APIs.')
    s = set()
    stripped_apis = []
    for api in apis:
        # Bullet is checked by the linter, no need to check here.
        a = api.lstrip('*- ').rstrip()
        s.add(a)
        stripped_apis.append(a)
        if '`' in a:
            raise Exception('API should not include backticks.')
    if '' in s:
        raise Exception('Empty line in APIs.')
    if len(apis) > len(s):
        raise Exception('Duplicate APIs.')
    if stripped_apis != sorted(stripped_apis, key=str.casefold):
        raise Exception('APIs are not sorted.')
    return s


def check_tags(tags_string: str) -> typing.Set[str]:
    """
    Check the format for `Tags` section.

    :param tags_string: A string containing all tags, with comma as delimiter.
    :return: A set of tags. Throws if format is wrong.
    """
    tags = tags_string.split(',')
    if not tags:
        raise Exception('Empty tags.')
    s = set()
    stripped_tags = []
    for tag in tags:
        t = tag.strip()
        s.add(t)
        stripped_tags.append(t)
        if t.lower() != t and t.upper() != t and t.capitalize() != t \
                and t not in exception_proper_nouns:
            raise Exception(f'Wrong letter case for tag: "{t}".')
    if '' in s:
        raise Exception('Empty char in tags.')
    if ', '.join(stripped_tags) != tags_string.strip():
        raise Exception('Extra whitespaces in tags.')
    if len(tags) > len(s):
        raise Exception('Duplicate tags.')
    if stripped_tags != sorted(stripped_tags, key=str.casefold):
        raise Exception('Tags are not sorted.')
    return s


def check_sentence_case(string: str) -> None:
    """
    Check if a sentence follows 'sentence case'. A few examples below.

    Hello world! -> YES
    I'm a good guy. -> YES
    a man and a gun. -> NO
    A WMS layer -> YES, as it's allowed to include proper nouns

    :param string: Input sentence, typically the title string.
    :return: None. Throws if is not sentence case.
    """
    # Check empty string.
    if not string:
        raise Exception('Empty title string.')
    # The whole sentence get excepted.
    if string in exception_proper_nouns:
        return
    # Split sentence into words.
    words = string.split()
    # First word should either be Title-cased or a proper noun (UPPERCASE).
    if words[0][0].upper() != words[0][0] and words[0].upper() != words[0] \
            and words[0] not in exception_proper_nouns:
        raise Exception('Wrong letter case for the first word in title.')
    # If a word is neither lowercase nor UPPERCASE then it is not great.
    for word in words[1:]:
        word = word.strip('()')
        if word.lower() != word and word.upper() != word \
                and word not in exception_proper_nouns:
            raise Exception(f'Wrong letter case for word: "{word}" in title.')


def check_is_subsequence(list_a: typing.List[str],
                         list_b: typing.List[str]) -> int:
    """
    Check if list A is a subsequence of list B.
    E.g.
    list_a = ['a', 'b', 'c']
    list_b = ['a', 'h', 'b', 'g', 'd', 'c']
    -> returns 0, which means all elements in list_a is also in list_b

    :param list_a: A list of strings, presumably the section titles of a README.
    :param list_b: A list of strings, presumably all valid titles in order.
    :return: 0 if list_a is subsequence of list_b.
    """
    # Empty list is always a subsequence of other lists.
    if not list_a:
        return True
    pa = len(list_a)
    for pb in range(len(list_b), 0, -1):
        pa -= 1 if list_b[pb-1] == list_a[pa-1] else 0
    return pa
# endregion


class ReadmeStyleChecker:

    essential_headers = {
        'Use case',
        'How to use the sample',
        'How it works',
        'Relevant API',
        'Tags'
    }

    possible_headers = [
        'Use case',
        'How to use the sample',
        'How it works',
        'Relevant API',
        'Offline data',
        'About the data',
        'Additional information',
        'Tags'
    ]

    def __init__(self, folder_path: str):
        self.folder_path = folder_path
        self.folder_name = get_folder_name_from_path(folder_path)
        self.readme_path = os.path.join(folder_path, 'README.md')
        self.readme_contents = None
        self.readme_parts = None
        self.readme_headers = None

    def populate_from_readme(self) -> None:
        """
        Read and parse the sections from README.

        :return: None. Throws if exception occurs.
        """
        try:
            readme_file = open(self.readme_path, 'r')
            # read the readme content into a string
            contents = readme_file.read()
            # A regular expression that matches exactly 2 pound marks, and
            # capture the trailing string.
            pattern = re.compile(r'^#{2}(?!#)\s(.*)', re.MULTILINE)
            self.readme_contents = contents
            # Use regex to split the README by section headers, so that they are
            # separated into paragraphs.
            self.readme_parts = re.split(pattern, contents)
            # Capture the section headers.
            self.readme_headers = re.findall(pattern, contents)
        except Exception as err:
            raise Exception(f'Error loading file - {self.readme_path} - {err}.')
        else:
            readme_file.close()

    def check_format_heading(self) -> None:
        """
        Check if
        1. essential section headers present.
        2. all sections are valid.
        3. section headers are in correct order.

        :return: None. Throws if exception occurs.
        """
        header_set = set(self.readme_headers)
        possible_header_set = set(self.possible_headers)
        # Check if all sections are valid.
        sets_diff = header_set - possible_header_set
        if sets_diff:
            raise Exception(
                f'Error header - Unexpected header or extra whitespace'
                f' - "{sets_diff}".')
        # Check if all essential section headers present.
        sets_diff = self.essential_headers - header_set
        if sets_diff:
            raise Exception(
                f'Error header - Missing essential header(s) - "{sets_diff}".')
        # Check if all sections are in correct order.
        index = check_is_subsequence(self.readme_headers, self.possible_headers)
        if index:
            raise Exception(
                f'Error header - Wrong order at - '
                f'"{self.readme_headers[index-1]}".')

    def check_format_title_section(self) -> None:
        """
        Check if
        1. the head has at least 3 parts (title, description and image URLs).
        2. the title string uses sentence case.

        :return: None. Throws if exception occurs.
        """
        try:
            title, _ = parse_head(self.readme_parts[0])
            check_sentence_case(title)
        except Exception as err:
            raise Exception(f'Error title - {err}')

    def check_format_apis(self) -> None:
        """
        Check if APIs
        1. do not have backticks.
        2. are sorted.
        3. do not have duplicate entries.

        :return: None. Throws if exception occurs.
        """
        try:
            api_section_index = self.readme_parts.index('Relevant API') + 1
            check_apis(self.readme_parts[api_section_index])
        except Exception as err:
            raise Exception(f'Error APIs - {err}')

    def check_format_tags(self) -> None:
        """
        Check if tags
        1. are in correct case.
        2. are sorted.
        3. do not have duplicate entries.

        :return: None. Throws if exception occurs.
        """
        try:
            tags_section_index = self.readme_parts.index('Tags') + 1
            check_tags(self.readme_parts[tags_section_index])
        except Exception as err:
            raise Exception(f'Error tags - {err}')

    def check_redundant_apis_in_tags(self) -> None:
        """
        Check if APIs and tags intersect.

        :return: None. Throws if exception occurs.
        """
        try:
            tags_section_index = self.readme_parts.index('Tags') + 1
            api_section_index = self.readme_parts.index('Relevant API') + 1
            api_set = check_apis(self.readme_parts[api_section_index])
            tag_set = check_tags(self.readme_parts[tags_section_index])
            if not api_set.isdisjoint(tag_set):
                raise Exception(f'Error tags - API should not be in tags')
        except Exception as err:
            raise Exception(f'Error checking extra tags due to previous error')


# region Main wrapper functions
def run_check(path: str, count: int) -> int:
    checker = ReadmeStyleChecker(path)
    # 1. Populate from README.
    try:
        checker.populate_from_readme()
    except Exception as err:
        count += 1
        print(f'{count}. {checker.folder_path} - {err}')
    # 2. Check format of headings, e.g. 'Use case', 'How it works', etc.
    try:
        checker.check_format_heading()
    except Exception as err:
        count += 1
        print(f'{count}. {checker.folder_path} - {err}')
    # 3. Check format of title section, i.e. title, description and image URLs.
    try:
        checker.check_format_title_section()
    except Exception as err:
        count += 1
        print(f'{count}. {checker.folder_path} - {err}')
    # 4. Check format of relevant APIs.
    try:
        checker.check_format_apis()
    except Exception as err:
        count += 1
        print(f'{count}. {checker.folder_path} - {err}')
    # 5. Check format of tags.
    try:
        checker.check_format_tags()
    except Exception as err:
        count += 1
        print(f'{count}. {checker.folder_path} - {err}')
    # 6. Check if redundant APIs in tags
    try:
        checker.check_redundant_apis_in_tags()
    except Exception as err:
        count += 1
        print(f'{count}. {checker.folder_path} - {err}')
    return count


def single(path: str):
    exception_count = run_check(path, 0)
    # Throw once if there are exceptions.
    if exception_count > 0:
        raise Exception('Error(s) occurred during checking a single design.')


def all_designs(path: str):
    exception_count = 0
    for root, dirs, files in os.walk(path):
        # Get parent folder name.
        parent_folder_name = get_folder_name_from_path(root)
        # If parent folder name is a valid category name.
        if parent_folder_name in categories:
            for dir_name in dirs:
                sample_path = os.path.join(root, dir_name)
                # Omit empty folders - they are omitted by Git.
                if len([f for f in os.listdir(sample_path)
                        if not f.startswith('.DS_Store')]) == 0:
                    continue
                exception_count = run_check(sample_path, exception_count)

    # Throw once if there are exceptions.
    if exception_count > 0:
        raise Exception('Error(s) occurred during checking all samples.')


def main():

    msg = 'README checker script. Run it against the /arcgis-ios-sdk-samples ' \
          'folder or a single sample folder. ' \
          'On success: Script will exit with zero. ' \
          'On failure: Style violations will print to console and the script ' \
          'will exit with non-zero code.'
    parser = argparse.ArgumentParser(description=msg)
    parser.add_argument('-a', '--all', help='path to project root folder')
    parser.add_argument('-s', '--single', help='path to a sample folder')
    args = parser.parse_args()
    if args.all:
        try:
            all_designs(args.all)
        except Exception as err:
            raise err
    elif args.single:
        try:
            single(args.single)
        except Exception as err:
            raise err
    else:
        raise Exception('Invalid arguments, abort.')
# endregion


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        print(f'{error}')
        # Abort with failure if any exception occurs.
        exit(1)
