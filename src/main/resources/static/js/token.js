const token = searchParam('token')

if (token) { // 로컬스토리지에 엑세스 토큰 등록
    localStorage.setItem("access_token", token)
}
function searchParam(key) { // 쿼리파라미터에서 엑세스토큰 값 가져오기
    return new URLSearchParams(location.search).get(key);
}