#!/bin/sh

echo "Compute the current version..."

version=$1

if [ "$version" = "" ]
then
  version="`grep "^version" gradle.properties | cut -d= -f2 | tr -d "[:space:]"`-SNAPSHOT"
fi

echo "Computed current version: $version"

repository_name=`git rev-parse --show-toplevel | xargs basename`

echo "Clone $repository_name..."
git clone https://github.com/springcard/$repository_name.git

cd $repository_name

echo "Checkout gh-pages branch..."
git checkout -f gh-pages

echo "Delete existing SNAPSHOT directory..."
rm -rf *-SNAPSHOT

echo "Delete existing RC directories in case of final release..."
rm -rf $version-rc*

echo "Create target directory $version..."
mkdir $version

echo "Copy dokka doc files..."
cp -rf ../springcard-pcsclike-plugin/build/dokka/html/* $version/

echo "Update versions list..."
echo "| Version | Documents |" > list_versions.md
echo "|:---:|---|" >> list_versions.md
for directory in `ls -rd [0-9]*/ | cut -f1 -d'/'`
do
  echo "| $directory | [API documentation]($directory)$diagrams |" >> list_versions.md
done

echo "Computed all versions:"
cat list_versions.md

cd ..

echo "Local docs update finished."



