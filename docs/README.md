
# Basic setup for gatsby-theme-apollo

This repo builds a simple version of the Gatsby theme for [Apollo docs](https://github.com/apollographql/gatsby-theme-apollo/tree/master/packages/gatsby-theme-apollo-docs)

The setup was derived from this helpful "hello world" [example](
https://codesandbox.io/s/gatsby-theme-apollo-docs-hello-world-bywp2?file=/package.json) by @trevorblades

## Build steps
<Add steps here>

## Issues

* My test setup based on the theme README was throwing errors. I will need to build up to that example from this basic example.
  * component errors
  The example in the read me requires version tags in your git commits, and other additional steps. This simplified version is enought to get started. The features branch adds component shadowing and sidebars.

  * needed to make dummy mdx and md files
  The code was throwing an error that seemed to be resolved by the presence of both mdx and md files.

* I had trouble adding the theme with Yarn on MacOSX and needed to add puppeteer first with a Chromium flag

```
PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true yarn add puppeteer
```
OR
```
PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true npm install puppeteer
```
