echo "stoping all"
docker stop c8db9e92456a
docker stop 4cb5fec0a489
docker stop 881d58d37bb8
docker stop bdf1a4b9cf1c
echo "stoped all"
echo "starting them"
docker start 4cb5fec0a489
docker start c8db9e92456a
docker start 881d58d37bb8
docker start bdf1a4b9cf1c
echo "started all"
docker ps -a
pause