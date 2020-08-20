// const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });


let functions = require('firebase-functions');

// gives us access to methods like referencing fb database
let admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

// function name is sendNotification and trigger is "onWrite" to db ref and ret data snap and context ref access params in ref fields in {}
exports.sendNotification = functions.database.ref('/chatrooms/{chatroomId}/chatroom_messages/{chatmessageId}')
.onWrite((snap, context) => {
	
	// log ouput to be printed on functions part of the fb project
	console.log("System: starting");
	console.log("snapshot: ", snap);
	console.log("snapshot.after: ", snap.after);
	console.log("snapshot.after.val(): ", snap.after.val());
	
	//get the message that was written
	let message = snap.after.val().message;
	let messageUserId = snap.after.val().user_id;
	console.log("message: ", message);
	console.log("user_id: ", messageUserId);
	
	//get the chatroom id
	let chatroomId = context.params.chatroomId;
	console.log("chatroom_id: ", chatroomId);
	
	
	// every query to db must be started with return in the funtion and here snap is snapshot ret
	return snap.after.ref.parent.parent.once('value').then(snap => {
		// use snap to get content of users node in chatroom main node
		let data = snap.child('users').val();
		console.log("data: ", data);
		
		//get the number of users in the chatroom
		let length = 0;
		for(value in data){
			length++;
		}
		console.log("data length: ", length);
		
		//loop through each user currently in the chatroom
		let tokens = [];
		let i = 0;
		// iters through and get all the users id in chatroom
		for(var user_id in data){
			console.log("user_id: ", user_id);
			
			//get the token(via the users id retrieved and query main user node) and add device token it to the array(tokens) 
			let reference = admin.database().ref("/users/" + user_id);
			reference.once('value').then(snap => {
				//get the token of device
				let token = snap.child('messaging_token').val();
				console.log('token: ', token);
				tokens.push(token);
				i++;
				
				//also check to see if the user_id we're viewing is the user who posted the message
				//if it is, then save that name so we can pre-pend it to the message
				let messageUserName = "";
				if(snap.child('user_id').val() === messageUserId){
					messageUserName = snap.child('name').val();
					console.log("message user name: " , messageUserName);
					// e.g neo : hey there boy
					message = messageUserName + ": " + message;				
				}
				
				//Once the last user in the list has been added we can continue
				if(i === length){          	// when iter through each user in chatroom.
					
					console.log("Construction the notification message.");
					
					// fcm creation and no need for headers since sent from fb servers
					const payload = {
						
						data: {
							data_type: "data_type_chat_message",
							title: "Tabian Consulting",
							message: message,
							chatroom_id: chatroomId
						}
					};
					
					// sends the messages to token in token arrays
					return admin.messaging().sendToDevice(tokens, payload)
						.then(function(response) {
							// See the MessagingDevicesResponse reference documentation for
							// the contents of response.
							console.log("Successfully sent message:", response);
						  })
						  .catch(function(error) {
							console.log("Error sending message:", error);
						  });
				}
			});
			
		}
	});
});
