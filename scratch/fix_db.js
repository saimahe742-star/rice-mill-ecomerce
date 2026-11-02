const mysql = require('mysql2/promise');

async function fix() {
  const connection = await mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'root123',
    database: 'rice_mill_db'
  });

  const [rows] = await connection.execute('SELECT id, name, image_url FROM products');
  console.log(rows);
  await connection.end();
}

fix().catch(console.error);
