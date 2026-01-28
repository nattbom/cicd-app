pipeline {
  agent any

  environment {
    NEXUS_REGISTRY = "192.168.70.10:5000"
    IMAGE_NAME     = "demo/springboot-backend"
    IMAGE_TAG      = "${env.BUILD_NUMBER}"
    SONAR_HOST_URL = "http://192.168.70.10:9000"
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Unit Test') {
      steps { sh './mvnw -q test' }
    }

    stage('SonarQube Scan') {
      steps {
        withCredentials([string(credentialsId: 'sonar', variable: 'SONAR_TOKEN')]) {
          sh """
            ./mvnw -q sonar:sonar \
              -Dsonar.host.url=${SONAR_HOST_URL} \
              -Dsonar.login=${SONAR_TOKEN}
          """
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        sh "docker build -t ${NEXUS_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
        sh "docker tag ${NEXUS_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ${NEXUS_REGISTRY}/${IMAGE_NAME}:latest"
      }
    }

    stage('Push Image to Nexus') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
          sh """
            echo "${NEXUS_PASS}" | docker login ${NEXUS_REGISTRY} -u "${NEXUS_USER}" --password-stdin
            docker push ${NEXUS_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
            docker push ${NEXUS_REGISTRY}/${IMAGE_NAME}:latest
          """
        }
      }
    }

    stage('Deploy Blue/Green') {
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'deploy', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
          sh """
            set -e

            # 1) หา live server จาก HAProxy (ดู weight ใน config runtime)
            LIVE=\$(echo "show servers state" | sudo socat stdio /run/haproxy/admin.sock | grep be_app | awk -F',' '{print \$4,\$18}' | sed 's/ //g' | awk -F',' '{print \$1}')
            # วิธีข้างบนอาจต่างกันตาม format; วิธีง่ายกว่า: อ่านจาก haproxy.cfg ว่าตอนนี้ตัวไหน weight 100
            # สำหรับ lab ใช้วิธีคงที่: ถ้า app1 เป็น live ก็ deploy app2 แล้ว flip (คุณต่อยอดทีหลัง)

            # กำหนดแบบง่าย: deploy ไป app2 ก่อนเสมอ ถ้าต้องการ robust จะเขียน logic เพิ่ม
            TARGET_IP="192.168.153.13"
            STANDBY_NAME="app2"
            LIVE_NAME="app1"

            echo "Deploying to standby: \$STANDBY_NAME (\$TARGET_IP)"

            ssh -i "\$SSH_KEY" -o StrictHostKeyChecking=yes \$SSH_USER@\$TARGET_IP '
              set -e
              sudo mkdir -p /opt/app
              sudo chown -R $USER:$USER /opt/app

              echo "Login registry"
              echo "${NEXUS_PASS}" | docker login ${NEXUS_REGISTRY} -u "${NEXUS_USER}" --password-stdin

              docker pull ${NEXUS_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}

              # stop old container if exists
              docker rm -f backend || true

              # run new
              docker run -d --name backend -p 8080:8080 \
                -e "app.version=${IMAGE_TAG}" \
                ${NEXUS_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
            '

            echo "Health check standby..."
            for i in \$(seq 1 30); do
              if curl -fsS http://\$TARGET_IP:8080/actuator/health | grep -q '"status":"UP"'; then
                echo "Standby is UP"
                break
              fi
              sleep 2
              if [ "\$i" -eq 30 ]; then
                echo "Standby not healthy"
                exit 1
              fi
            done

            echo "Flip HAProxy to make standby live"
            echo "set server be_app/${LIVE_NAME} weight 0"   | sudo socat stdio /run/haproxy/admin.sock
            echo "set server be_app/${STANDBY_NAME} weight 100" | sudo socat stdio /run/haproxy/admin.sock

            echo "Done. New live: ${STANDBY_NAME}"
          """
        }
      }
    }
  }
}
