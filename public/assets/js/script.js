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

var provider = new firebase.auth.GoogleAuthProvider();

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
        
      }).catch((error) => {
        console.error(error);
      });
  });

  $(".menu .logout").click(function () {
    firebase.auth().signOut().then(() => {
      // Sign-out successful.
    }).catch((error) => {
      console.error(error);
    });
  });

});

function loadInterface() {
  if (firebaseUser != null) {
    $(".menu .name img").attr({ src: firebaseUser.photoURL });
    $(".menu .name span").html(firebaseUser.displayName);
    $(".menu .logout").show();
    $(".menu .login").hide();
  } else {
    $(".menu .name").html("");
    $(".menu .logout").hide();
    $(".menu .login").show();
  }
}