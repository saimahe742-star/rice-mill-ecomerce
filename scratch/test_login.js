const fetch = require('node-fetch');

async function test() {
    try {
        const res = await fetch('http://localhost:8082/api/auth/customer/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone: '9715639592', name: 'leena' })
        });
        const body = await res.text();
        console.log('Status:', res.status);
        console.log('Body:', body);
    } catch (e) {
        console.error('Error:', e);
    }
}

test();
