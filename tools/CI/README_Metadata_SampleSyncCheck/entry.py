#!/usr/bin/env python3

import subprocess as sp

def main():

    print("** Starting sample sync **")
    sp.call(f'python3 /samplesync_change_checker.py',  shell=True)


if __name__ == '__main__':
    main()