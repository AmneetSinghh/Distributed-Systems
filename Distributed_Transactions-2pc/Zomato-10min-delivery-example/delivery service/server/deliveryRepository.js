const {Pool} = require("pg")

const pool = new Pool({
    "host": "localhost",
    "port": 5433,
    "user":"postgres",
    "password" : "postgres",
    "database" : "postgres",
})

// We have fixed amount of delivery workers.....

// dont assign order.
async function reserveAgent(){
    try{
        const con = await pool.connect();
        await con.query("BEGIN");
        let res = await con.query("SELECT * FROM delivery WHERE is_reserved = FALSE AND order_id IS NULL LIMIT 1 FOR UPDATE")//  if this runs, no txn can edit these selected rows.
        if (res.rowCount == 0){
            await con.query("ROLLBACK");
            con.release();
            return {result : null, error : "No Delivery Boy available"}
        }
        let agentId = res.rows[0].id
        // Get current timestamp in milliseconds
        const currentTimestamp = Date.now();
        // Add 2 minutes (2 * 60 * 1000 milliseconds) to the current timestamp
        const reservationExpiry = currentTimestamp + 2 * 60 * 1000;
    
        res = await con.query("update delivery set is_reserved = TRUE, reservation_expiry = $1 where id = $2",[reservationExpiry,agentId])
        // res = await con.query('",[food_id])
        await con.query("COMMIT"); // Commit the transaction
        con.release();
        return {result : agentId, error : null} // returning packet id
    } catch(e){
        return {result : null, error : e} // returning packet id
    }
    
}

async function bookAgent(order_id){
    const con = await pool.connect();
    await con.query("BEGIN");
    // select first available.
    let res = await con.query("SELECT * FROM delivery WHERE is_reserved = TRUE AND order_id IS NULL LIMIT 1 FOR UPDATE")
    if (res.rowCount == 0){
        await con.query("ROLLBACK");
        con.release();
        return {result : null, error : "No Delivery Boy available"}
    }
    let agentID = res.rows[0].id
    // Get current timestamp in milliseconds
    const currentTimestamp = Date.now();
    // Add 10 minutes (10 * 60 * 1000 milliseconds) to the current timestamp
    const orderExpiry = currentTimestamp + 10 * 60 * 1000;

    // not reserved but assigned order_id.
    res = await con.query("update delivery set is_reserved = FALSE, reservation_expiry = NULL, order_id = $1, order_expiry=$2 where id = $3",[order_id,orderExpiry,agentID])
    // res = await con.query('",[food_id])
    await con.query("COMMIT"); // Commit the transaction
    con.release();
    return {result : agentID, error : null} // returning packet id
}

module.exports={
    reserveAgent,
    bookAgent
}