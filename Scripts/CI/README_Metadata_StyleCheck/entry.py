#!/usr/bin/env python3

import os
import json
import argparse
import subprocess as sp

# A set of category folder names in current sample viewer.
# Only run the checks when a file path is within one of these category folders.
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


def run_mdl(readme_path: str):
    print("**** mdl ****")
    code = sp.call(f'mdl --style /style.rb "{readme_path}"', shell=True)
    return code


def run_style_check(dirname: str):
    print("**** README_style_checker ****")
    code1 = sp.call(f'python3 /README_style_checker.py -s "{dirname}"', shell=True)
    //print("**** title_differ ****")
    //code2 = sp.call(f'python3 /title_differ.py -s "{dirname}"', shell=True)
    print("**** description_differ ****")
    code3 = sp.call(f'python3 /description_differ.py -s "{dirname}"', shell=True)
    print("**** metadata_style_checker ****")
    code4 = sp.call(f'python3 /metadata_style_checker.py -s "{dirname}"', shell=True)
    return code1 + code2 + code3 + code4


def read_json(filenames_json_data):
    return [filename for filename in filenames_json_data]


def load_json_file(path: str):
    try:
        json_file = open(path, 'r')
        json_data = json.load(json_file)
    except Exception as err:
        print(f'Error reading JSON - {path} - {err}')
        raise err
    else:
        json_file.close()
    return json_data


def main():
    msg = 'Entry point of the docker to run mdl and style check scripts.'
    parser = argparse.ArgumentParser(description=msg)
    parser.add_argument('-s', '--string', help='A JSON array of file paths.')
    args = parser.parse_args()
    files = None

    print("** Starting checks **")
    if args.string:
        files = read_json(json.loads(args.string))
        if not files:
            print('Invalid input file paths string, abort.')
            exit(1)
    else:
        print('Invalid arguments, abort.')
        exit(1)

    return_code = 0
    # A set of dirname strings to avoid duplicate checks on the same sample.
    samples_set = set()

    for f in files:
        if not os.path.exists(f):
            # The changed file is deleted, no need to style check.
            continue

        path_parts = os.path.normpath(f).split(os.path.sep)

        if len(path_parts) < 3:
            # A file not in samples folder, omit.
            # E.g. might be in the root folder or other unrelated folders.
            continue

        # Get filename and folder name of the changed sample.
        filename = os.path.basename(f)
        dir_path = os.path.dirname(f)
        l_name = filename.lower()

        # Print debug information for current sample.
        if dir_path not in samples_set:
            print(f'*** Checking {dir_path} ***')

        # Check if the capitalization of doc filenames are correct.
        if l_name == 'readme.md' and filename != 'README.md':
            print(f'Error: {dir_path} filename has wrong capitalization')
            return_code += 1
            continue
        if l_name == 'readme.metadata.json' and filename != 'README.metadata.json':
            print(f'Error: {dir_path} filename has wrong capitalization')
            return_code += 1
            continue

        # Run the markdownlint linter on README file.
        if filename == 'README.md':
            # Run the linter on markdown file.
            return_code += run_mdl(f)

        # Run the other Python checks on the whole sample folder.
        if dir_path not in samples_set:
            samples_set.add(dir_path)
            return_code += run_style_check(dir_path)

        # Changed file is not a README or metadata file, omit.

    if return_code != 0:
        # Non-zero code occurred during the process.
        exit(return_code)
    else:
        exit(0)


if __name__ == '__main__':
    main()
