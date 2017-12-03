
# publish.sh <testcases>

NAME=cs132
TESTCASES=$1

git archive --format tar --prefix=$NAME/ HEAD | tar -xC /tmp/
git -C $TESTCASES archive --format tar --prefix=$NAME/testcases/ master | tar -xC /tmp/

tar zcf $NAME.tgz -C /tmp $NAME

rm -rf /tmp/$NAME
