#!/bin/env bash

# Welcome to the CS132 grading script

# Write info
info() {
    [[ $VERBOSITY -gt 1 ]] && echo "$@" 1>&2;
}
export -f info

# Warn about something
warn() {
    [[ $VERBOSITY -gt 0 ]] && echo "WARN: $@" 1>&2;
}
export -f warn

# This is a failing problem.
fail() {
    SUID=$1; shift
    echo "FAIL: $@" 1>&2;
    echo "$SUID" 0
}
export -f fail

function grade () {
    filename=$1
    name=$(basename $filename)
    SUID=${name%.*}

    info "Testing that $SUID is a student id."
    # Testing that student id is correct.
    if [[ ! $SUID =~ ^[0-9]{9}$ ]]; then
        fail "$SUID" "Bad student id '$SUID'"
        return
    fi


    WORK_DIR="$RESULT_FOLDER/$SUID"
    mkdir -p "$WORK_DIR/src"

    info "Untaring into '$WORK_DIR/src'"
    # unfolding
    tar -xf "$filename" -C "$WORK_DIR/src"

    cd "$WORK_DIR"

    set +e

    mkdir classes

    info "Compiling sources."

    javac -sourcepath src "src/$MAINCLASS.java" -d classes 2>&1 > compile_output
    if [[ $? != 0 ]]
    then
        warn "Error in $WORK_DIR/compile_output"
        fail "$SUID" "Couldn't compile project"
        return
    fi

    GRADE=0

    mkdir -p "tests"

    touch succeeded failed

    find "$TEST_FILES" -type f -not -name "*.out" \
    | while IFS= read -r file
    do
        info "Testing '$file'"
        outfile="tests/$(basename $file).out"
        DIFF_FILE="tests/$(basename $file).diff"

        java -cp classes $MAINCLASS <"$file" >"$outfile"

        if [[ ! -f "$file.out" ]]; then
            warn "$file.out does not exist"
        else
            diff "$file.out" "$outfile" > "$DIFF_FILE"

            if [[ $? == 0 ]]; then
                rm "$DIFF_FILE"
                echo "$file.out" >> succeeded
            else
                warn "Failed '$file'"
                echo "$file.out" >> failed
            fi
        fi
    done
    set -e
    echo "$SUID" "$(wc -l <succeeded)"
}
export -f grade

if [ $# != 3 ]
then
    echo "Welcome to the CS132 grading script:

Usage:
  grade <home-work> <test-folder> <tar-folder>
  grade <home-work> <test-folder> <tar-file>
"
fi

HOME_WORK=$1; shift;

case $HOME_WORK in
    "hw1")
        MAINCLASS="Parse"
        ;;
    "hw2")
        MAINCLASS="Typecheck"
        ;;
    *) 
        warn "Unreconized homework '$HOME_WORK'"
        ;;
esac

TEST_FILES=$(cd "$(dirname "$1")" && pwd)/$(basename "$1"); shift;
TAR_FOLDER=$1; shift;

RESULT_FOLDER="results"
export RESULT_FOLDER TEST_FILES MAINCLASS

if [[ -e $RESULT_FOLDER ]];
then
    rm -r "$RESULT_FOLDER"
fi

mkdir "$RESULT_FOLDER"

COUNT=$(find "$TEST_FILES" -name '*.out' | wc -l)

if [[ -f "$TAR_FOLDER" ]]; then
    # If the tar folder is a function
    VERBOSITY=2
    export VERBOSITY

    info "Testing '$TAR_FOLDER' against all $COUNT tests"
    info "Results in '$RESULT_FOLDER'"

    # Throw away the results, as the final grade will have more test-cases
    grade "$TAR_FOLDER"

else
    # If the tar folder is a folder, run all of them
    VERBOSITY=1
    export VERBOSITY

    # Grade all items in the tar folder:
    find "$TAR_FOLDER" -name "*.tar" \
        | sort \
        | xargs -n 1 -I {} bash -c 'grade "$@"' _ {}
fi
