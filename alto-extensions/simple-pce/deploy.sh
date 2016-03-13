DISTRO_DIR=$1

cp features/target/alto-spce-features-1.0.0-SNAPSHOT.kar $DISTRO_DIR/deploy/
mkdir tmp
unzip features/target/alto-spce-features-1.0.0-SNAPSHOT.kar -d tmp
cp -r tmp/repository/org $DISTRO_DIR/system/
rm -rf tmp
