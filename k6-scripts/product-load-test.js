import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');
const BASE_URL = 'http://localhost:8080/api/v1';

// 상품 API만 테스트
export const options = {
  stages: [
    { duration: '30s', target: 20 },   // 30초동안 0->20 사용자
    { duration: '1m', target: 50 },    // 1분동안 20->50 사용자
    { duration: '2m', target: 50 },    // 2분동안 50 사용자 유지
    { duration: '30s', target: 0 },    // 30초동안 0으로 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내
    errors: ['rate<0.05'],            // 에러율 5% 미만
  },
};

export default function () {
  // 1. 상품 목록 조회 (캐시 테스트)
  const pageNum = Math.floor(Math.random() * 10);
  const listResponse = http.get(
    `${BASE_URL}/products?page=${pageNum}&size=10&sort=latest`
  );
  
  const listOk = check(listResponse, {
    'list status 200': (r) => r.status === 200,
  });
  
  const listTimeOk = check(listResponse, {
    'list time < 500ms': (r) => r.timings.duration < 500,
  });
  
  if (!listOk) {
    errorRate.add(1);
    console.log(`List API failed: ${listResponse.status} - ${listResponse.body?.substring(0, 100)}`);
  }
  sleep(0.5);

  // 2. 상품 상세 조회 (실제 존재하는 ID 범위)
  const productId = Math.floor(Math.random() * 1000) + 2931; // 2931-3931 범위
  const detailResponse = http.get(
    `${BASE_URL}/products/${productId}`
  );
  
  const detailOk = check(detailResponse, {
    'detail status 200': (r) => r.status === 200,
  });
  
  const detailTimeOk = check(detailResponse, {
    'detail time < 300ms': (r) => r.timings.duration < 300,
  });
  
  if (!detailOk) {
    errorRate.add(1);
    console.log(`Detail API failed for product ${productId}: ${detailResponse.status}`);
  }
  sleep(0.5);
}

export function setup() {
  const res = http.get('http://localhost:8081/actuator/health');
  if (res.status !== 200) {
    throw new Error('서버가 실행 중이 아닙니다.');
  }
  console.log('서버 헬스체크 통과');
}
