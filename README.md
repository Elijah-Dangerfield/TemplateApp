# Welcome

### This project is a template used to make setting up new projects less painful. 

#### New projects based off this template include these codebase features: 

  - A build-logic module with gradle convention plugins for your modules needs
  - A git commit hook to run detekt and checkstyle static code analysis as well as a build check to ensure the hooks are installed before building
  - Opinionated CI workflows leveraging Github actions. [Read more about the CI/CD setup](https://github.com/Elijah-Dangerfield/templateapp/blob/main/docs/ci.md)
  - Version Cataloging with basic dependencies included
  - A module creation script (scripts/create_module.main.kts) to make creating modules under a `/core` or `/features` module quick and simple
    
## How To

To use this template you can follow these steps:


### 1. Clone the repository
### 2. Use the script to reate a new codebase based off the template
run the `setup.main.kts`script located in the project root and pass in the name of the new project you would like to create
`./setup.main.kts <APP_NAME>`
    
### 3. Set up signing config for CI: 

Steps:
           
- Generate a key store named `release.keystore`, write down the alias, key store password and key password. 
  save those as github secrets named **KEYALIAS** **KEYSTORE_PASSWORD** and **KEY_PASSWORD** respectively
- run: `gpg -c --armor release.keystore`
- enter whatever passphrase you want and save it as a github secret under **RELEASE_KEYSTORE_PASSPHRASE**
- copy and past the content from the generated asc file into a secret named **RELEASE_KEYSTORE**
           
Doing this will allow CI to sign the app.
           
### 4. Create a repo scoped token
On github under `settings` -> `developer settings` -> `personal access tokens`, create a fine grained
access token for your repo with access to read and write at least pull requests. 
Save that token as a secret in the repository named **REPO_SCOPED_TOKEN**
           
### 5. Set permissions for Github actions
On github under project -> settings -> actions -> general, set the Workflow permissions to be read & write
           
### 6. Setup gradle in the new project
run ` gradle wrapper` in the new project (otherwise gradle is upset)
youll also want to make all scripts in the new project executable by running : `chmod u+x ./scripts/**/*.kts`
