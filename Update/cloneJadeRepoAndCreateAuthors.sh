mkdir jade && cd jade;
svn co https://jade.tilab.com/svn/jade/trunk/;
cd trunk;
svn info --show-item revision > actRevNumber.txt;
svn log --xml --quiet | grep author | sort -u | \
            perl -pe 's/.*>(.*?)<.*/$1 = /' > authors.txt;
cd ..;
cd ..;
find jade/trunk/authors.txt -type f -print0 | xargs -0 mv -t .;
find jade/trunk/actRevNumber.txt -type f -print0 | xargs -0 mv -t .;
rm -rf jade;
java -jar editAuthorfile.jar  $PWD/authors.txt

