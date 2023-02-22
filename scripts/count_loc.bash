parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" || fail ; pwd -P )
cd "$parent_path"/..

find \
 './Blacksmith/src' \
 './blacksmith-docs/docs' \
 './blacksmith-docs/src' \
 './scripts' \
 \( -not -name '*.java' \) -print0 | xargs --null wc -l
