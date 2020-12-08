#!/usr/bin/env bash

SSN_DIR='../main/resources/data/PRPA_IN201306UV02/profile'
README_FILE='../../README.md'

echo "# Mock MPI Data Offerings" > $README_FILE
echo "" >> $README_FILE
echo "### PRPA_IN201306UV02" >> $README_FILE
echo "| First Name | Last Name | SSN | ICN |" >> $README_FILE
echo "|------|-----|-------|-------|" >> $README_FILE

for file in `ls $SSN_DIR`; do
    SSN="$(echo $file | cut -f1 -d'.')"
    ICN=$(cat $SSN_DIR/$file | grep 'NI^200M^USVHA^P' | sed -e 's/<id root="2.16.840.1.113883.4.349" extension="\(.*\)^NI^200M^USVHA^P"/\1/' | cut -f1 -d"/" | xargs)

    SUBJECT_NAME="$(awk '/<livingSubjectName>/{p=1}p' $SSN_DIR/$file)"
    FIRST_NAME=$(echo $SUBJECT_NAME | awk -F "<given>" '{print $2}' | cut -f1 -d"<")
    LAST_NAME=$(echo $SUBJECT_NAME | awk -F "<family>" '{print $2}' | cut -f1 -d"<")

    echo "| $FIRST_NAME | $LAST_NAME | $SSN | $ICN |" >> $README_FILE
done

