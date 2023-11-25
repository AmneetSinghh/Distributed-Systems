const axios = require("axios");

// use multithreading for calling these functions.....
async function placeOrder(orderID) {
  try {
    for (let i = 1; i <= 20; i++) {
      console.log("-------------------------------------- "+ i +" ---------------------------------------")
      console.log("---- Packet Reserve ----")
      // atomic
      await networkCall(
        "http://localhost:8081/reserve-food",
        "Packet Reserved: ",
        {}
      );
      console.log("---- Delivery Boy Reserve ----")
      // atomic
      await networkCall(
        "http://localhost:8082/reserve-agent",
        "Agent Reserved: ",
        {}
      );
      console.log("---- Packet Book ----")
      // atomic
      await networkCall("http://localhost:8081/book-food", "Packet booked: ", {
        orderID: i,
      });
      console.log("---- Delivery Boy Book ----")
      // atomic
      await networkCall("http://localhost:8082/book-agent", "Agent booked: ", {
        orderID: i,
      });
    }
  } catch (e) {
    throw new Error("Order not Placed: ", e)
  }
}

async function networkCall(url, message, body) {
 try{
    let res = await axios.post(url,body)
    res = res.data
    console.log(JSON.stringify(res))
    if (res.error != null) {
        throw new Error(res.error);
      }
 } catch(e){
    throw new Error("Connection errror" + e)
 }
}

placeOrder();
