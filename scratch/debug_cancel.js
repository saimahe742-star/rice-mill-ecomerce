const http = require('http');

async function testCancel() {
    // 1. Login as customer
    const loginData = JSON.stringify({ phone: '9715639592', name: 'leena' });
    const loginOptions = {
        hostname: 'localhost',
        port: 8082,
        path: '/api/auth/customer/login',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': loginData.length
        }
    };

    let cookie = '';
    const loginRes = await new Promise((resolve) => {
        const req = http.request(loginOptions, (res) => {
            cookie = res.headers['set-cookie'][0].split(';')[0];
            resolve(res);
        });
        req.write(loginData);
        req.end();
    });

    console.log('Login Status:', loginRes.statusCode);

    // 2. Get my orders to find one to cancel
    const listOptions = {
        hostname: 'localhost',
        port: 8082,
        path: '/api/orders/mine',
        method: 'GET',
        headers: {
            'Cookie': cookie
        }
    };

    const ordersBody = await new Promise((resolve) => {
        http.get(listOptions, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve(JSON.parse(body)));
        });
    });

    const pendingOrder = ordersBody.find(o => o.status === 'PENDING');
    if (!pendingOrder) {
        console.log('No PENDING order found to cancel.');
        return;
    }

    console.log('Target Order ID:', pendingOrder.id);

    // 3. Try to cancel
    const cancelOptions = {
        hostname: 'localhost',
        port: 8082,
        path: `/api/orders/${pendingOrder.id}/cancel`,
        method: 'POST',
        headers: {
            'Cookie': cookie
        }
    };

    const cancelRes = await new Promise((resolve) => {
        const req = http.request(cancelOptions, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => resolve({ status: res.statusCode, body }));
        });
        req.end();
    });

    console.log('Cancel Status:', cancelRes.status);
    console.log('Cancel Body:', cancelRes.body);
}

testCancel();
