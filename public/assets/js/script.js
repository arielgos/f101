const firebaseConfig = {
  apiKey: "AIzaSyAeT-TyrKZ8tlaWT4aBOCn744zoOcPrF8U",
  authDomain: "f101-80e53.firebaseapp.com",
  projectId: "f101-80e53",
  storageBucket: "f101-80e53.appspot.com",
  messagingSenderId: "777561954223",
  appId: "1:777561954223:web:56aaf8fe66a1b8b770415a",
  measurementId: "G-0D6H2J3Y9B"
};
firebase.initializeApp(firebaseConfig);

var provider = new firebase.auth.GoogleAuthProvider();

firebase.auth().onAuthStateChanged((user) => {
  if (user) {
    console.log(user.displayName);
  }
});

$(document).ready(function () {
  console.log("App Start");
  $("html").click(function () {
    console.log("Body click");
    firebase.auth()
      .signInWithPopup(provider)
      .then((result) => {
        var credential = result.credential;
        var token = credential.accessToken;
        var user = result.user;
        console.log(user.displayName);
      }).catch((error) => {
        console.error(error);
      });
  });
});