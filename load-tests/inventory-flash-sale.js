import http from 'k6/http';
import { check } from 'k6';

http.setResponseCallback(http.expectedStatuses(201, 409));

export const options = {
  scenarios: {
    flash_sale: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 500),
      timeUnit: '1s',
      duration: __ENV.DURATION || '30s',
      preAllocatedVUs: 500,
      maxVUs: 1000,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1000'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const sku = __ENV.SKU || 'FLASH-SALE-SKU';
const orderId = __ENV.ORDER_ID || '00000000-0000-0000-0000-000000000001';

export default function () {
  const response = http.post(
    `${baseUrl}/api/inventory/reservation`,
    JSON.stringify({ orderId, sku, quantity: 1 }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': `k6-${__VU}-${__ITER}`,
      },
    },
  );

  check(response, {
    'reserved or rejected without overselling': (r) => r.status === 201 || r.status === 409,
  });
}
