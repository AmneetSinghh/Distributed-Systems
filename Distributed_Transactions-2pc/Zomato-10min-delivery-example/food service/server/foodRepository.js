const {Pool} = require("pg")

const pool = new Pool({
    "host": "localhost",
    "port": 5433,
    "user":"postgres",
    "password" : "postgres",
    "database" : "postgres",
})



// dont assign order.
async function reserveFood(food_id){
    try{
        const con = await pool.connect();
        await con.query("BEGIN");
        let res = await con.query("SELECT * FROM packets WHERE is_reserved = FALSE AND food_id = $1 and order_id IS NULL LIMIT 1 FOR UPDATE",[food_id])
        if (res.rowCount == 0){
            await con.query("ROLLBACK");
            con.release();
            return {result : null, error : "No Food Packet available"}
        }
        let packetID = res.rows[0].id
        // Get current timestamp in milliseconds
        const currentTimestamp = Date.now();
        // Add 2 minutes (2 * 60 * 1000 milliseconds) to the current timestamp
        const reservationExpiry = currentTimestamp + 2 * 60 * 1000;
    
        res = await con.query("update packets set is_reserved = TRUE, reservation_expiry = $1 where id = $2",[reservationExpiry,packetID])
        // res = await con.query('",[food_id])
        await con.query("COMMIT"); // Commit the transaction
        con.release()
        return {result : packetID, error : null} // returning packet id
    }
     catch(e){
        console.log(e);
        return {result : null, error : e} // returning packet id
     }
}

async function bookFood(food_id,order_id){
    const con = await pool.connect();
    await con.query("BEGIN");
    // select first available.
    let res = await con.query("SELECT * FROM packets WHERE is_reserved = TRUE AND food_id = $1 and order_id IS NULL LIMIT 1 FOR UPDATE",[food_id])
    if (res.rowCount == 0){
        await con.query("ROLLBACK");
        con.release();
        return {result : null, error : "No Food Packet available"}
    }
    let packetID = res.rows[0].id
    // Get current timestamp in milliseconds
    const currentTimestamp = Date.now();
    // Add 10 minutes (10 * 60 * 1000 milliseconds) to the current timestamp
    const orderExpiry = currentTimestamp + 10 * 60 * 1000;

    // not reserved but assigned order_id.
    res = await con.query("update packets set is_reserved = FALSE, reservation_expiry = NULL, order_id = $1, order_expiry=$2 where id = $3",[order_id,orderExpiry,packetID])
    // res = await con.query('",[food_id])
    await con.query("COMMIT"); // Commit the transaction
    con.release()
    return {result : packetID, error : null} // returning packet id
}



module.exports={
    reserveFood,
    bookFood
}