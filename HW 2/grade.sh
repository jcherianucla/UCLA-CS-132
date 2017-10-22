#!/bin/env bash

# Welcome to the CS132 grading script
# Author: Christian Kalhauge <kalhauge@cs.ucla.edu>

# Write info
info() {
    (( VERBOSITY > 1 )) && echo "$@" 1>&2;
}
export -f info

# Warn about something
warn() {
    (( VERBOSITY > 0 )) && echo "WARN: $@" 1>&2;
}
export -f warn

# This is a failing problem.
fail() {
    SUID=$1; shift
    SHA256=$1; shift
    echo "FAIL: $@" 1>&2;
    echo "$SUID" 0 "$SHA256"
}
export -f fail

function grade () {
    filename="$1"
    name=$(basename "$filename")
    SUID=${name%.*}

    SHA256=$(sha256sum < "$filename" | cut -f1 -d' ')

    info "Testing that $SUID is a student id."
    # Testing that student id is correct.
    if [[ ! $SUID =~ ^[0-9]{9}$ ]]; then
        fail "$SUID" "$SHA256" "Bad student id '$SUID'"
        return
    fi

    WORK_DIR="$RESULT_FOLDER/$SUID"
    mkdir -p "$WORK_DIR/src"

    info "Untaring into '$WORK_DIR/src'"
    # unfolding
    tar -xf "$filename" -C "$WORK_DIR/src"

    info "Testing tar content."

    pushd "$WORK_DIR/src" > /dev/null
    find . -type f -print0 | while IFS= read -r -d $'\0' line; do
        info "Checking $line"
        extension="${line##*.}"
        if [ ! "$extension" = "java" ]; then
            fail "$SUID" "$SHA256" "Found non-java file '$line'"
            return -1
        elif grep -Fx "$line" "$ILLEGAL_FILES" > /dev/null; then
            fail "$SUID" "$SHA256" "Found illegal file in tar '$line'"
            return -1
        fi
    done

    if [ "$?" != "0" ]
    then
        popd > /dev/null
        return
    fi
    popd > /dev/null

    cd "$WORK_DIR"

    mkdir classes

    info "Compiling sources."

    set +e
    javac -sourcepath src -cp "$LIBRARIES" "src/$MAINCLASS.java" -d classes 2>compile_output
    if [ "$?" != "0" ]
    then
        warn "Error in $WORK_DIR/compile_output"
        fail "$SUID" "$SHA256" "Couldn't compile project"
        return
    fi

    GRADE=0

    mkdir -p "tests"

    touch succeeded failed

    find "$TEST_FILES" -type f -not -name "*.out" \
    | while IFS= read -r file
    do
        info "Testing '$file'"
        OUT_FILE="tests/$(basename "$file").out"
        ERR_FILE="tests/$(basename "$file").err"
        DIFF_FILE="tests/$(basename "$file").diff"

        timeout $TIMEOUT java \
           -Djava.security.manager -Djava.security.policy=="misc/JavaSecurityPolicy.policy" \
           -cp "$LIBRARIES" "$MAINCLASS" <"$file" >"$OUT_FILE" 2>"$ERR_FILE"

        if [ "$?" == "124" ]; then
            warn "Timeout after $TIMEOUT"
        fi

        if [[ ! -f "$file.out" ]]; then
            warn "$file.out does not exist"
        else
            diff "$OUT_FILE" "$file.out" > "$DIFF_FILE"

            if [ "$?" == "0" ]; then
                rm "$DIFF_FILE"
                echo "$file.out" >> succeeded
            else
                warn "Failed '$file'"
                if (( VERBOSITY == 2 )); then
                    cat "$ERR_FILE" 1>&2
                    cat "$DIFF_FILE" 1>&2
                fi
                echo "$file.out" >> failed
            fi
        fi
    done
    set -e
    SUCC=$(wc -l <succeeded) 
    percent=$((200*$SUCC/$COUNT % 2 + 100*$SUCC/$COUNT))
    echo "$SUID" "$percent" "$SHA256"
}
export -f grade

if [ $# != 4 ]
then
    echo "Welcome to the CS132 grading script:

Usage:
  grade <homework> <grade-folder> <test-folder> <tar-folder>
  grade <homework> <grade-folder> <test-folder> <tar-file>
"
exit -1
fi


HOMEWORK=$1; shift;
GRADE_FOLDER=$(cd "$(dirname "$1")" && pwd)/$(basename "$1"); shift;
LIB_FOLDER="$GRADE_FOLDER/lib"
MISC_FOLDER="$GRADE_FOLDER/misc"
ILLEGAL_FILES="$MISC_FOLDER/illegalfiles"
TIMEOUT=5s

case $HOMEWORK in
    "hw1")
        MAINCLASS="Parse"
        LIBRARIES="classes"
        ILLEGAL_FILES="/dev/null"
        ;;
    "hw2")
        MAINCLASS="Typecheck"
        LIBRARIES="classes:$LIB_FOLDER/minijava-parser.jar"
        ;;
    *)
        warn "Unreconized homework '$HOMEWORK'"
        ;;
esac

TEST_FILES=$(cd "$(dirname "$1")" && pwd)/$(basename "$1"); shift;
TAR_FOLDER=$1; shift;

RESULT_FOLDER="results"

if [[ -e $RESULT_FOLDER ]];
then
    rm -r "$RESULT_FOLDER"
fi

mkdir "$RESULT_FOLDER"

COUNT=$(find "$TEST_FILES" -name '*.out' | wc -l)
export RESULT_FOLDER TEST_FILES MAINCLASS LIBRARIES ILLEGAL_FILES TIMEOUT COUNT

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
