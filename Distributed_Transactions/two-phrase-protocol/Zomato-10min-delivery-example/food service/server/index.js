const express = require('express');
const app = express();
const {reserveFood, bookFood} = require("./foodRepository")
// Middleware to parse JSON in the request body
app.use(express.json());

// reserve food item.
app.post("/reserve-food",async(req,res)=>{
    console.log("reserve Food ")
    const { result, error } = await reserveFood(1)
    console.log("its resered", result, error)
    if(error == null){
        res.send({status : 200, res : result, error : error})
    }
    else{
        res.send({status : 500, res : result, error : error})
    }
})


// assign food
app.post("/book-food",async(req,res)=>{
    console.log("book-food : ")
    const orderID = req.body.orderID
    console.log("Food Booking order ID : "+ orderID)
    const { result, error }= await bookFood(1,orderID)
    if(error == null){
        res.send({status : 200, res : result, error : error})
    }
    else{
        res.send({status : 500, res : result, error : error})
    }
})

app.listen(8081,()=>{
    console.log("Listening on port 8081")
})