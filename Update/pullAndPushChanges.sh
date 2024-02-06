git config svn.authorsfile $PWD/authors.txt;
RevNumber=$(grep -Eo '[0-9\.]+' actRevNumber.txt);
sub=20;
mkdir jade;
cd jade;
git svn init https://jade.tilab.com/svn/jade/trunk/;
RevNumber=$(($RevNumber-$sub));
git svn fetch -r$RevNumber:HEAD;
git remote add JadeEnflexit https://github.com/EnFlexIT/JADE.git;
git fetch JadeEnflexit;
$SHELL
