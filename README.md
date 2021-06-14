# With Me on Android v5.0

프로젝트 결과물을 보는 것에 초점을 맞추기 위해 안드로이드 버전 4.4는 잠시 보류하고, 우선 5.0에서 CameraX를 이용해 카메라를 구현하기로 하였습니다.\
카메라에 사용되는 CameraX API를 사용하기 위해서는 해당 API가 지원하는 최소 버전인 5.0에 맞출 필요가 있었습니다.

CameraX로 개발한 카메라에서 촬영한 사진을 서버로 전송 후, 해당 사진에 대한 response를 받아오는 앱을 개발하는 것이 목적입니다.

사실 더 일찍 개발중이었지만 깃허브 조작 도중 pull 실수로 파일이 싹 증발해 다시 개발하였습니다.

## 개발 과정

### 6.14
- 기본 안드로이드 프로젝트 생성
- CameraX 프로젝트로부터 커스텀 카메라 코드 가져오기