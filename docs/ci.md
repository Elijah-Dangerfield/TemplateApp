# CI/CD


This project includes a basic yet opinionated CI/CD system leveraging Github Actions. 
The workflows for these can be found under `.github/workflows`

## The CI
The continuous integration system runs on all pull requests automatically ensuring code is less smelly and that breaking changes do not get merged in

For each PR the system runs: 
- Static code analysis on every module with Detekt and Checkstyle
- Unit Tests on all modules
- A debug and release build which get linked to the pull request

Updates to pull requests cancel any ongoing ci workflows for that pr and re run the 
ci system again. 


## The CD

The goal of the CD system in this template is to make creating release simple. So simple that opening an IDE should not be needed

The CD system setup by default is for Github releases only. If you wish to hook your project up to automatically deploy to an app store you will need to make a new workflow. Its likely best to create one that publishes to an app store when drafts are published on the repo. 

Creating a release can be broken into 2 steps:

1. Create the release branch
2. Publish the release draft


### Create the release branch
Creating a release branch is just a click of a button. 

On the github repo's top bar you can navigate to `actions` -> `Create Release Branch`

From there you just need to run the workflow by clicking the `Run Workflow` button and providing a version for the release. 

![Screen Shot 2023-02-16 at 10 37 24 PM](https://user-images.githubusercontent.com/45648517/219713573-7bad2660-d0e3-4cac-bbdd-d00d091211d4.png)


After doing this the CD system will update the `version code` and the `version name` in the code base, commit those changes and create a PR. 

The CI system will them run tests, static code analysis and create builds for the new release. 

Once this is done a draft release will be created and linked to the PR along with the release artifacts. 

![template v1 0 into main #4](https://user-images.githubusercontent.com/45648517/219713673-5c8e8518-e3c0-4449-babd-5588979b1906.png)



### Publish the release draft

After the CI system creates a draft release you are 2 steps away from creating a release. 

You can find the draft by following the link that CI added to the release PR or by navigating to `tags` -> `drafts` on github

Upon opening the draft you should see a message to update the release notes along with all of the release artifacts that CI generated

![Screen Shot 2023-02-16 at 11 04 40 PM](https://user-images.githubusercontent.com/45648517/219713705-c6eb302e-f5e2-48d3-b224-4b96720e9d10.png)


From there all you need to do is 

1. Edit the release notes to describe your release
2. Click the green `Publish` button at the bottom of the release draft

Congratulations! You just made your a successful release!

At this point the draft is considered published and you will be able to find this release under the `releases` section on Github. 

![Screen Shot 2023-02-16 at 11 07 46 PM](https://user-images.githubusercontent.com/45648517/219713755-d5861af6-fc32-487c-a0e5-363f27f56762.png)



If you set up more CD to publish to an app store when drafts are published then those should be started. Otherwise you can use the assets from the draft to release to an app store.
