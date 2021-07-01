var firebaseUser = null;

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
firebase.analytics();

var firestore = firebase.firestore();
var provider = new firebase.auth.GoogleAuthProvider();

//listener de la sesion
firebase.auth().onAuthStateChanged((user) => {
  if (user) {
    firebaseUser = user;
  } else {
    firebaseUser = null;
  }
  loadInterface();
});

$(document).ready(function () {
  console.log("App Start");
  //inicio de sesion
  $(".menu .login").click(function () {
    firebase.auth()
      .signInWithPopup(provider)
      .then((result) => {
        firebaseUser = result.user;
        firestore.collection("users")
          .doc(firebaseUser.uid)
          .set({
            id: firebaseUser.uid,
            user: firebaseUser.displayName,
            image: firebaseUser.photoURL,
          }).then(function (docRef) {
            console.log("Document written with ID: ", docRef.id);
          }).catch(function (error) {
            //console.error("Error adding document: ", error);
          });
      }).catch((error) => {
        console.error(error);
      });
  });

  //cerrar sesion
  $(".menu .logout").click(function () {
    firebase.auth().signOut().then(() => {
      firebaseUser = null;
    }).catch((error) => {
      console.error(error);
    });
  });

  //listener de usuarios
  firestore.collection("users")
    .onSnapshot(function (querySnapshot) {
      querySnapshot.docChanges().forEach(function (change) {
        if (change.type === "added") {
          createUser(change.doc.data());
        }
      });
    });

  $(".loading").toggle();

});

function loadInterface() {
  console.log("loading interface...");
  if (firebaseUser !== null) {
    console.log(firebaseUser.uid);
    $(".menu .name img").attr({ src: firebaseUser.photoURL });
    $(".menu .name span").html(firebaseUser.displayName);
    $(".menu .logout").show();
    $(".menu .login").hide();
    $(".chat").show();
    $(".users").css("width", "70%");
  } else {
    $(".menu .name img").attr({ src: "" });
    $(".menu .name span").html("");
    $(".menu .logout").hide();
    $(".menu .login").show();
    $(".chat").hide();
    $(".users").css("width", "100%");
  }
}

function createUser(data) {
  var div = $("<div>").attr({ class: "user" });
  div.append("<img src='" + data.image + "'>");
  div.append("<span>" + data.user + "</span>");
  $(".users").append(div);
  animateUser(div);
}

function animateUser(obj) {
  var top = Math.floor(Math.random() * $(".users").height());
  var left = Math.floor(Math.random() * $(".users").width() - 100);
  obj.animate({
    top: top + "px",
    left: left + "px"
  }, 1500, function () {
    animateUser(obj)
  });
}