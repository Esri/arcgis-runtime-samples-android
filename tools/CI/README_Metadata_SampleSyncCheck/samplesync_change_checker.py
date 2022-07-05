import subprocess
import os

def main():

    '''
    Usage: python sample_sync.py 
    '''

    readme_script_path = os.path.join(".","tools", "readme_copy", "readme_copy.py")
    metadata_script_path = os.path.join(".", "tools", "metadata_tools", "process_metadata.py")

    print("Copying readmes")
    subprocess.run(["python", readme_script_path])

    print("Updating metadata")
    subprocess.run(["python", metadata_script_path])

    return

if __name__ == "__main__":
    main()