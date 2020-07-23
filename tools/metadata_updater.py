import os
import re
import json
import typing
import argparse


def check_special_char(string: str) -> bool:
    """
    Check if a string contains special characters.

    :param string: The input string.
    :return: True if there are special characters.
    """
    # regex = re.compile('[@_!#$%^&*()<>?/\\|}{~:]')
    regex = re.compile('[@_!#$%^&*<>?|/\\}{~:]')
    if not regex.search(string):
        return False
    return True


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

    :param tags_string: A string containing all tags, with comma or newline as delimiter.
    :return: A sorted list of stripped tags.
    """
    tags = re.split(r'[,\n]', tags_string)
    if not tags:
        raise Exception('README Tags parse failure!')
    tags = [x for x in tags if x != '']
    return sorted([tag.strip() for tag in tags])


def get_folder_name_from_path(path: str) -> str:
    """
    Get the folder name from a full path.

    :param path: A string of a full/absolute path to a folder.
    :return: The folder name.
    """
    return os.path.normpath(path).split(os.path.sep)[-1]


class MetadataUpdater:

    def __init__(self, folder_path: str, single_update: bool = False):
        """
        The standard format of metadata.json for Android platform. Read more at:
        https://devtopia.esri.com/runtime/common-samples/wiki/README.metadata.json
        """
        self.category = ''          # Populate from json.
        self.description = ''       # Populate from README.
        self.formal_name = ''       # Populate from README.
        self.ignore = False         # Default to False.
        self.images = []            # Populate from folder paths.
        self.keywords = []          # Populate from README.
        self.language = ''          # Populate from folder paths.
        self.provision_from = []    # Populate from json.
        self.provision_to = []      # Populate from json.
        self.redirect_from = []     # Populate from json.
        self.relevant_apis = []     # Populate from README.
        self.snippets = []          # Populate from folder paths.
        self.title = ''             # Populate from README.

        self.folder_path = folder_path
        self.folder_name = get_folder_name_from_path(folder_path)
        self.readme_path = os.path.join(folder_path, 'README.md')
        self.json_path = os.path.join(folder_path, 'README.metadata.json')

        self.single_update = single_update

    def get_source_code_paths(self) -> typing.List[str]:
        """
        Traverse the directory and get all filenames for source code.
        Ignores any code files in the `/build/` directory.

        :return: A list of java or kotlin source code filenames starting from `/src/`.
        """
        results = []
        for dp, dn, filenames in os.walk(self.folder_path):
            if ("/build/" not in dp):
                for file in filenames:
                    extension = os.path.splitext(file)[1]
                    if extension in ['.java'] or extension in ['.kt']:
                        # get the programming language of the sample
                        self.language = 'java' if extension in ['.java'] else 'kotlin'
                        # get the snippet path
                        snippet = os.path.join(dp, file)
                        if snippet.startswith(self.folder_path):
                            # add 1 to remove the leading slash
                            snippet = snippet[len(self.folder_path):]
                        results.append(snippet)
        if not results:
            raise Exception('Unable to get java/kotlin source code paths.')
        return sorted(results)

    def get_images_paths(self):
        """
        Traverse the directory and get all filenames for images in the top level directory.

        :return: A list of image filenames.
        """
        results = []
        list_subfolders_with_paths = [f.name for f in os.scandir(self.folder_path) if f.is_file()]
        for file in list_subfolders_with_paths:
            if os.path.splitext(file)[1].lower() in ['.png']:
                results.append(file)
        if not results:
            raise Exception('Unable to get images paths.')
        return sorted(results)

    def populate_from_json(self) -> None:
        """
        Read 'category', 'redirect_from', 'provision_to', and 'provision_from'
        fields from json, as they should not be changed.
        """
        try:
            json_file = open(self.json_path, 'r')
            json_data = json.load(json_file)
        except Exception as err:
            print(f'Error reading JSON - {self.json_path} - {err}')
            raise err
        else:
            json_file.close()

        keys = json_data.keys()
        for key in ['category']:
            if key in keys:
                setattr(self, key, json_data[key])
        if 'redirect_from' in keys:
            if isinstance(json_data['redirect_from'], str):
                self.redirect_from = [json_data['redirect_from']]
            elif isinstance(json_data['redirect_from'], typing.List):
                self.redirect_from = json_data['redirect_from']
            else:
                print(f'No redirect_from in - {self.json_path}, abort.')
        if 'provision_from' in keys:
            if isinstance(json_data['provision_from'], str):
                self.provision_from = [json_data['provision_from']]
            elif isinstance(json_data['provision_from'], typing.List):
                self.provision_from = json_data['provision_from']
            else:
                print(f'No provision_from in - {self.json_path}, abort.')
        if 'provision_to' in keys:
            if isinstance(json_data['provision_to'], str):
                self.provision_to = [json_data['provision_to']]
            elif isinstance(json_data['provision_to'], typing.List):
                self.provision_to = json_data['provision_to']
            else:
                print(f'No provision_to in - {self.json_path}, abort.')

    def populate_from_readme(self) -> None:
        """
        Read and parse the sections from README, and fill in the 'title',
        'description', 'relevant_apis' and 'keywords' fields in the dictionary
        for output json.
        """
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
            # create a formal name key from a pascal case version of the title
            # with parentheses removed.
            formal_name = ''.join(x for x in self.title.title() if not x.isspace())
            self.formal_name = re.sub('[()]','', formal_name)

            if check_special_char(self.title + self.description):
                print(f'Info: special char in README - {self.folder_name}')
            self.relevant_apis = parse_apis(readme_parts[api_section_index])
            keywords = parse_tags(readme_parts[tags_section_index])
            # Do not include relevant apis in the keywords
            self.keywords = [w for w in keywords if w not in self.relevant_apis]

            # This is left in from the iOS script:
            # "It combines the Tags and the Relevant APIs in the README."
            # See /runtime/common-samples/wiki/README.metadata.json#keywords
            # self.keywords += self.relevant_apis
        except Exception as err:
            print(f'Error parsing README - {self.readme_path} - {err}.')
            raise err

    def populate_from_paths(self) -> None:
        """
        Populate source code and image filenames from a sample's folder.
        """
        try:
            self.images = self.get_images_paths()
            self.snippets = self.get_source_code_paths()
        except Exception as err:
            print(f"Error parsing paths - {self.folder_name} - {err}.")
            raise err

    def flush_to_json(self, path_to_json: str) -> None:
        """
        Write the metadata to a json file.

        :param path_to_json: The path to the json file.
        """
        data = dict()

        if not self.category and self.single_update:
            data["category"] = "TODO"
        else:
            data["category"] = self.category

        data["description"] = self.description
        data["formal_name"] = self.formal_name
        data["ignore"] = self.ignore
        data["images"] = self.images
        data["keywords"] = self.keywords
        data["language"] = self.language

        if self.provision_from:
             data["provision_from"] = self.provision_from
        elif self.single_update:
            data["provision_from"] = "TODO"

        if self.provision_to:
             data["provision_to"] = self.provision_to
        elif self.single_update:
            data["provision_to"] = "TODO"

        if self.redirect_from and self.redirect_from[0] is not '':
            data["redirect_from"] = self.redirect_from
        elif self.single_update:
            data["redirect_from"] = "TODO"

        data["relevant_apis"] = self.relevant_apis
        data["snippets"] = self.snippets
        data["title"] = self.title

        with open(path_to_json, 'w+') as json_file:
            json.dump(data, json_file, indent=4, sort_keys=True)
            json_file.write('\n')


def update_1_sample(path: str):
    """
    Fixes 1 sample's metadata by running the script on a single sample's directory.
    """
    single_updater = MetadataUpdater(path, True)
    try:
        single_updater.populate_from_json()
        single_updater.populate_from_readme()
        single_updater.populate_from_paths()
    except Exception:
        print(f'Error populate failed for - {single_updater.folder_name}.')
        return
    single_updater.flush_to_json(os.path.join(path, 'README.metadata.json'))


def main():
    # Initialize parser.
    msg = 'Metadata helper script. Run it against the top level folder of an ' \
          'Android platform language (ie. kotlin or java) with the -m flag ' \
          'or against a single sample using the -s flag and passing in eg. kotlin/my-sample-dir'
    parser = argparse.ArgumentParser(description=msg)
    parser.add_argument('-m', '--multiple', help='input directory of the language')
    parser.add_argument('-s', '--single', help='input directory of the sample')
    args = parser.parse_args()

    if args.multiple:
        category_root_dir = args.multiple
        category_name = get_folder_name_from_path(category_root_dir)
        print(f'Processing category - `{category_name}`...')

        list_subfolders_with_paths = [f.path for f in os.scandir(category_root_dir) if f.is_dir()]
        for current_path in list_subfolders_with_paths:
            print(current_path)
            updater = MetadataUpdater(current_path)
            try:
                updater.populate_from_json()
                updater.populate_from_readme()
                updater.populate_from_paths()
            except Exception:
                print(f'Error populate failed for - {updater.folder_name}.')
                continue
            updater.flush_to_json(updater.json_path)
    elif args.single:
        update_1_sample(args.single)
    else:
        update_1_sample()
        print('Invalid arguments, abort.')


if __name__ == '__main__':
    # Use main function for a full category.
    main()
    # Use test function for a single sample.
    # update_1_sample()
