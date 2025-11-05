# Automatic Release Workflow

## Overview

The `release-on-tag.yml` workflow automatically creates GitHub releases with generated changelogs when a new tag is pushed to the repository.

## How It Works

### Trigger

The workflow is triggered automatically when any tag is pushed to the repository:

```bash
git tag v1.0.0
git push origin v1.0.0
```

### What It Does

1. **Checks out the repository** with full history to enable changelog generation
2. **Extracts the tag name** from the Git reference
3. **Generates release notes** automatically using GitHub's release notes generation API
4. **Creates a GitHub release** with:
   - Release title: `Release <tag-name>`
   - Auto-generated changelog based on commits since the last tag
   - Automatically marks as pre-release if tag contains `-rc`, `-beta`, or `-alpha`

### Release Types

- **Pre-releases**: Tags containing `-rc`, `-beta`, or `-alpha` are automatically marked as pre-releases
- **Full releases**: All other tags are marked as full releases

## Usage Examples

### Creating a Pre-release

```bash
# Create and push a release candidate tag
git tag v1.73.0-rc.17
git push origin v1.73.0-rc.17
```

This will create a pre-release on GitHub with an auto-generated changelog.

### Creating a Full Release

```bash
# Create and push a release tag
git tag v1.73.0
git push origin v1.73.0
```

This will create a full release on GitHub with an auto-generated changelog.

## Changelog Generation

The workflow uses GitHub's built-in release notes generation API, which:
- Lists all pull requests merged since the last tag
- Categorizes changes by labels
- Credits contributors
- Provides links to full changelog and compare views

## Permissions

The workflow requires `contents: write` permission to create releases, which is automatically provided by the `GITHUB_TOKEN`.

## Customization

To customize the release notes format, you can add a `.github/release.yml` configuration file to the repository. See [GitHub's documentation](https://docs.github.com/en/repositories/releasing-projects-on-github/automatically-generated-release-notes) for more details.
