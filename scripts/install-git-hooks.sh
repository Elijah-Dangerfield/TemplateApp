#!/bin/bash

# options
quiet=$(( $([ "$1" == -q ] || [ "$1" == --quiet ]; echo $?) == 0 ))
helper=$(( $([ "$1" == -h ] || [ "$1" == --help ]; echo $?) == 0 ))

# validate current directory is a git repository
if [ ! -d .git ] && [ ! -d ../.git ]; then
    if (( !quiet )); then
        echo "Can't install hooks, not a git repository."
    fi
    exit
fi

if (( helper )); then
    echo "Install git hooks from this directory."
    echo "Usage: ./install.sh [option]"
    echo "-q, --quiet   install or fail quietly"
    echo "-h, --help    display this help and exit"
    exit
fi

path=$(git rev-parse --show-toplevel)
ln_options=

if (( quiet )); then
    ln_options="-sf" # symbolic, force
else
    ln_options="-siv" # symbolic, interactive, verbose
fi

# link all shell scripts in /hooks to .git/hooks
for hook in "$path/config/git-hooks"/*.sh; do
    name=$(basename "$hook" .sh)

    # create symbolic link for git hook
    ln $ln_options "$hook" "$path/.git/hooks/$name"
    chmod +x "$path/.git/hooks/$name"
done

if (( !quiet )); then
    echo "Git hooks installed successfully."
fi
