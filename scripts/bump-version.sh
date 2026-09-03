#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
VERSION_FILE="$ROOT_DIR/VERSION"

CURRENT_VERSION=$(cat "$VERSION_FILE" | tr -d '[:space:]')
echo "Current version: $CURRENT_VERSION"

if [[ "$CURRENT_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-modified\.([0-9]+))?$ ]]; then
    MAJOR="${BASH_REMATCH[1]}"
    MINOR="${BASH_REMATCH[2]}"
    PATCH="${BASH_REMATCH[3]}"
    MODIFIED_BUILD="${BASH_REMATCH[5]:-0}"
else
    echo "Invalid VERSION: $CURRENT_VERSION"
    exit 1
fi

case "${1:-modified}" in
    major) MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0; MODIFIED_BUILD=1 ;;
    minor) MINOR=$((MINOR + 1)); PATCH=0; MODIFIED_BUILD=1 ;;
    patch) PATCH=$((PATCH + 1)); MODIFIED_BUILD=1 ;;
    modified) MODIFIED_BUILD=$((MODIFIED_BUILD + 1)) ;;
    *) echo "Usage: $0 [modified|major|minor|patch]"; exit 1 ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH-modified.$MODIFIED_BUILD"

# Only update VERSION file - everything else reads from it:
#   - build.gradle.kts reads ../VERSION at build time
#   - build_mac.sh reads VERSION at build time
#   - release.yml reads VERSION at build time
#   - README badge auto-fetches from GitHub Release API
#   - Website auto-fetches from GitHub Release API
echo "$NEW_VERSION" > "$VERSION_FILE"

echo ""
echo "  $CURRENT_VERSION -> $NEW_VERSION"
echo ""
echo "Next steps:"
echo "  git add VERSION && git commit -m \"chore: bump version to $NEW_VERSION\""
echo "  git tag $NEW_VERSION && git push && git push origin $NEW_VERSION"
