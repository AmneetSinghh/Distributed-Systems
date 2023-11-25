const express = require('express');
const app = express();
const {reserveAgent, bookAgent} = require("./deliveryRepository")
// Middleware to parse JSON in the request body
app.use(express.json());
// reserve agent.
app.post("/reserve-agent",async(req,res)=>{
    const { result, error }= await reserveAgent(1)
    if(error == null){
        res.send({status : 200, res : result, error : error})
    }
    else{
        res.send({status : 500, res : result, error : error})
    }
})


// assign agent
app.post("/book-agent",async(req,res)=>{
    const orderID = req.body.orderID
    const { result, error }= await bookAgent(orderID)
    if(error == null){
        res.send({status : 200, res : result, error : error})
    }
    else{
        res.send({status : 500, res : result, error : error})
    }
})

const PORT = 8082
app.listen(PORT,()=>{
    console.log("Listening on port "+ PORT)
})