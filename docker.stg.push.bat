set TAG=%1
aws configure set aws_access_key_id AKIARLTBZ7EUNFRDIVFP
aws configure set aws_secret_access_key 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
aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 093620664616.dkr.ecr.ap-southeast-1.amazonaws.com
docker build --platform=linux/amd64 --no-cache -t srt-iotp-iam-ecr .
docker tag srt-iotp-iam-ecr:latest 093620664616.dkr.ecr.ap-southeast-1.amazonaws.com/srt-iotp-iam-ecr:%TAG%
docker push 093620664616.dkr.ecr.ap-southeast-1.amazonaws.com/srt-iotp-iam-ecr:%TAG%
