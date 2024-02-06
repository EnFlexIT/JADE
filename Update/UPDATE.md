## Download and Install GIT and SVN

1. **GIT Installer**: If you did not install GIT yet. Download the installer from the official [site](https://git-scm.com/downloads) and execute it.
2. **Subversion (SVN)**:
   - If you did not install SVN yet. Download the [VisualSvnServer Installer](https://git-scm.com/downloads) and execute it.
   - Ensure only the required Administration Tools are selected and add the command line tools to the PATH variable.

## Configure SVN

To connect to a secured SVN Server, you'll need to authenticate. Use your credentials for the JADE SVN server. For authentication issues, adjust `cloneJadeRepoAndCreateAuthors.sh` and `pullAndPushChanges.sh` scripts by adding `--username` and `--password` parameters with your credentials to checkout(co) commands in the scripts. 

## Execute the Scripts

Ensure `cloneJadeRepoAndCreateAuthors.sh`, `pullAndPushChanges.sh`, and `editAuthorfile.jar` are in the same folder. Execute `cloneJadeRepoAndCreateAuthors.sh` first, followed by `pullAndPushChanges.sh`.
The scripts can be found in the Update-folder of this repository.

## Rebasing on JADE-SVN

You can rebase on JADE-SVN using either `git rebase JadeEnflexit/JADE-SVN` or through Eclipse, with Eclipse being the recommended method for its interactivity and ease of handling potential issues.
Therefore you have to add the newly created Git repository to your Eclipse workspace or open a shell in the folder where you have executed the scripts.
