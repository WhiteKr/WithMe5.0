# With Me on Android v5.0

프로젝트 결과물을 보는 것에 초점을 맞추기 위해 안드로이드 버전 4.4는 잠시 보류하고, 우선 5.0에서 CameraX를 이용해 카메라를 구현하기로 하였습니다.\
카메라에 사용되는 CameraX API를 사용하기 위해서는 해당 API가 지원하는 최소 버전인 5.0에 맞출 필요가 있었습니다.

CameraX로 개발한 카메라에서 촬영한 사진을 서버로 전송 후, 해당 사진에 대한 response를 받아오는 앱을 개발하는 것이 목적입니다.

사실 더 일찍 개발중이었지만 깃허브 조작 도중 pull 실수로 파일이 싹 증발해 다시 개발하였습니다.

## 개발 과정

### ~6.14
- 기본 안드로이드 프로젝트 생성
- CameraX 프로젝트로부터 커스텀 카메라 코드 가져오기

혼돈을 막기 위해 따로 만들어둔 CameraX 프로젝트가 있었기 때문에 기본 틀을 잡는 데에는  큰 문제가 없었습니다.

### ~6.17
- 카메라 촬영 시 엘범에 저장하는 기능 제거
- 메모리에서 버퍼 이미지 불러와 화면에 표시
- 화면표시 제거 후 서버통신 개발 준비

ImageCapture.OnImageCapturedCallback 콜백 함수를 활용하는 과정에서 많은 삽질이 있었습니다.\
코틀린에서 콜백 함수를 사용해보는 것도 처음이었고, 버퍼와 이미지 파일, ImageProxy 다루기도 처음이었습니다.

여러 번의 검색과 실패 후 시행착오 끝에 버퍼 이미지를 불러와 프리뷰를 띄우는 데 성공했습니다.\
이후에는 불러온 이미지를 서버로 전송해 응답을 받고 텍스트로 출력하는 것이 목적입니다.
