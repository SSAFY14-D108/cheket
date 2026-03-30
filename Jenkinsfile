pipeline {
    agent any

    environment {
        // Docker 이미지 이름
        DOCKER_IMAGE = 'cheket-backend'
        DOCKER_TAG = "${BUILD_NUMBER}"

        // EC2 서버 정보
        EC2_HOST = 'j14d108.p.ssafy.io'
        EC2_USER = 'ubuntu'
        EC2_DEPLOY_PATH = '/home/ubuntu/cheket'

        // Jenkins에 등록한 Credentials ID
        EC2_SSH_KEY = 'ec2-ssh-key'           // EC2 PEM 키
        DOCKER_ENV_FILE = 'cheket-env-file'   // .env 파일 내용
    }

    stages {
        // 1단계: 코드 체크아웃
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // 2단계: Backend 빌드 & Docker 이미지 생성
        stage('Build Docker Image') {
            steps {
                dir('backend') {
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        // 3단계: Docker 이미지를 tar로 저장
        stage('Save Docker Image') {
            steps {
                sh "docker save ${DOCKER_IMAGE}:latest -o ${DOCKER_IMAGE}.tar"
            }
        }

        // 4단계: EC2로 전송 & 배포
        stage('Deploy to EC2') {
            steps {
                sshagent(credentials: [EC2_SSH_KEY]) {
                    // 배포 디렉토리 생성
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                            mkdir -p ${EC2_DEPLOY_PATH}
                        '
                    """

                    // Docker 이미지 전송
                    sh """
                        scp -o StrictHostKeyChecking=no \
                            ${DOCKER_IMAGE}.tar \
                            ${EC2_USER}@${EC2_HOST}:${EC2_DEPLOY_PATH}/
                    """

                    // docker-compose.yml 전송
                    sh """
                        scp -o StrictHostKeyChecking=no \
                            docker-compose.yml \
                            ${EC2_USER}@${EC2_HOST}:${EC2_DEPLOY_PATH}/
                    """

                    // .env 파일 전송 (Jenkins Credentials에서)
                    withCredentials([file(credentialsId: DOCKER_ENV_FILE, variable: 'ENV_FILE')]) {
                        sh """
                            scp -o StrictHostKeyChecking=no \
                                \$ENV_FILE \
                                ${EC2_USER}@${EC2_HOST}:${EC2_DEPLOY_PATH}/.env
                        """
                    }

                    // EC2에서 배포 실행
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                            cd ${EC2_DEPLOY_PATH}

                            # Docker 이미지 로드
                            docker load -i ${DOCKER_IMAGE}.tar

                            # 기존 컨테이너 중지 & 새 컨테이너 시작
                            docker compose down backend || true
                            docker compose up -d backend redis

                            # 사용하지 않는 이미지 정리
                            docker image prune -f

                            # tar 파일 삭제 (용량 절약)
                            rm -f ${DOCKER_IMAGE}.tar
                        '
                    """
                }
            }
        }

        // 5단계: 헬스체크
        stage('Health Check') {
            steps {
                sshagent(credentials: [EC2_SSH_KEY]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                            echo "Waiting for application to start..."
                            sleep 30

                            # 컨테이너 상태 확인
                            docker ps | grep cheket_backend

                            # 헬스체크 (Spring Actuator)
                            curl -f http://localhost:8080/actuator/health || echo "Health check failed!"
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo '✅ 배포 성공!'
        }
        failure {
            echo '❌ 배포 실패!'
        }
        always {
            // Jenkins 서버의 임시 파일 정리
            sh "rm -f ${DOCKER_IMAGE}.tar || true"
            // 사용하지 않는 Docker 이미지 정리
            sh "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
        }
    }
}
