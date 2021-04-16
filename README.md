# KAU 산학프로젝트 - Industry-academia project
### Main APP : KAUSH


### APP 요약
1. 안드로이드 클라이언트는 Android Studio로 개발했다.
2. 클라이언트에서 Google speech-to-text를 이용해 음성 변환을 진행한다.
3. 클라이언트와 Amazon S3를 연결하기 위해 AWS Cognito를 사용했다.
4. Amazon S3에 사용자가 음성으로 입력한 텍스트 데이터가 파일 형태로 저장된다.
5. AWS Lambda가 S3의 객체 생성 이벤트를 감지하고 S3에 새로 생성된 객체에 접근한다.
6. 텍스트 파일 내부의 텍스트 데이터를 읽어와 SageMaker에서 배포 중인 학습 모델로 전달한다.
7. SageMaker에서 확률값을 반환하면 해당 값을 Amazon API Gateway Endpoint로 배포한다.
8. 안드로이드 클라이언트가 Amazon API Gateway Endpoint에 접근해 확률값을 가져온다.
9. 여러 개인 데이터들은 Firebase에 저장되어 필요할 때 시각화 자료로 사용된다.