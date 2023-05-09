mvn clean
mvn initialize

OPTIONS=""

if [ "$1" == "--skip-tests" ]
then
    OPTIONS="$OPTIONS -Dmaven.test.skip=true"
fi

mvn package $OPTIONS
