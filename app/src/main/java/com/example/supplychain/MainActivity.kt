package com.example.supplychain

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.supplychain.ui.theme.SupplyChainTheme
import com.example.supplychainapp.Transaction
import com.example.supplychainapp.User
import com.example.supplychainapp.UserViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            val transactionViewModel: TransactionViewModel = viewModel()
            MyApp(userViewModel, transactionViewModel)

        }
    }
}


@Composable
fun MyApp(userViewModel: UserViewModel, transactionViewModel: TransactionViewModel) {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        showSplash = false
    }
    if (showSplash) {
        SplashScreen()
    } else {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {

                LoginScreen(navController, userViewModel, transactionViewModel) }
            composable(
                route = "createTransaction/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId")
                CreateTransactionScreen(navController, userId, transactionViewModel)
            }

//            composable("transactionTabs") {backStackEntry ->
//                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
//                TransactionTabsScreen(transactionViewModel, navController = navController, userId = userId)
//            }

            composable(
                route = "transactionDetails/{transactionId}/{userId}",
                arguments = listOf(
                    navArgument("transactionId") { type = NavType.IntType },
                    navArgument("userId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: 0
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                TransactionDetailsScreen(navController = navController, transactionId = transactionId, userId = userId, transactionViewModel)
            }

            composable("scanTransaction/{transactionId}", arguments = listOf(navArgument("transactionId") { type = NavType.StringType })) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")
                ScanTransactionScreen(navController, transactionId)
            }
        }
    }
}


@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB8DBD9)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.logo), // Change this to your logo resource ID
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BC Lab",
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFF000000)

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, userViewModel: UserViewModel, transactionViewModel: TransactionViewModel) {

//    , userViewModel: UserViewModel = viewModel(),transactionViewModel: TransactionViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current // Add this line to get the current context

    var errorMessage by remember { mutableStateOf("") }

    var loginResult by remember { mutableStateOf<Int?>(null) }

    val stringOfQRCode = Array(2) { Array(9) { "" } }
    stringOfQRCode[0][0] = "2fce72c0-ca35-47da-b3ae-39fd1b2d2f2f"
    stringOfQRCode[1][0] = "f57c04fa-990f-4d57-a2b8-aa5d19b391ef"
    stringOfQRCode[0][1] = "a3c7f0e7-52d1-4689-ac7b-797c7a959ce8"
    stringOfQRCode[1][1] = "5900a8d9-cd66-4177-8b90-a12907177392"
    stringOfQRCode[0][2] = "0a90f749-a792-4da8-a783-38abebe15f3d"
    stringOfQRCode[1][2] = "2507666c-acc4-4633-8074-e9987c9688e5"
    stringOfQRCode[0][3] = "d04fc240-516b-4b83-8a9f-627309fa13b1"
    stringOfQRCode[1][3] = "6313da51-70b4-4e81-ac45-b80d149cd019"
    stringOfQRCode[0][4] = "7a5531c1-f7fe-4a37-96b8-7f649a627c0c"
    stringOfQRCode[1][4] = "568cdb04-7d5c-4f60-b62b-a43ed5ff44df"
    stringOfQRCode[0][5] = "3ab8daa5-eeda-4e7f-8549-c8a1fa69e5cd"
    stringOfQRCode[1][5] = "97fe70f1-4604-4d1d-946a-3e110f06081e"
    stringOfQRCode[0][6] = "c2361a4d-bc00-4cfb-826f-1255a7c82828"
    stringOfQRCode[1][6] = "2d77e15f-81d6-483e-8c2d-428c43987c3e"
    stringOfQRCode[0][7] = "face0042-901a-4764-a8c5-96d5ef9b6589"
    stringOfQRCode[1][7] = "f11171c3-4e29-4e26-92ab-9f6f25c83c15"
    stringOfQRCode[0][8] = "c5e14281-f811-4ff1-a8bc-a55233162ae8"
    stringOfQRCode[1][8] = "dc36e603-5f95-479f-9422-f899d2d72abd"
//    userViewModel.deleteAllUsers()
//    userViewModel.resetUserIdSequence()
//    transactionViewModel.deleteAllTransactions()

//    val userId by userViewModel.userId

//    val users = (1..10).map { User(username = "User$it", password = "Password$it") }
//    userViewModel.insertUsers(users)
//    val userList by userViewModel.allUsers.collectAsState()

//    transactionViewModel.deleteAllTransactions()

//    val transactions = (1..5).map{ Transaction (id = it, firstUserId = it, secondUserId = it + 1, firstQRCode = stringOfQRCode[0][it - 1], secondQRCode = stringOfQRCode[1][it - 1], firstUserApproved = false, secondUserApproved = false, status = "Pending")}
//    transactionViewModel.insertTransactions(transactions)

//    val transactions = (6..9).map{ Transaction (id = it, firstUserId = it, secondUserId = it + 1, firstQRCode = stringOfQRCode[0][it - 1], secondQRCode = stringOfQRCode[1][it - 1], firstUserApproved = false, secondUserApproved = false, status = "Pending")}
//    transactionViewModel.insertTransactions(transactions)

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(0.dp)
                    .fillMaxSize()
                    .background(Color(0xFF515151)) // Background color set to #C0C0C0
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .padding(top = 50.dp)
                        .padding(start = 30.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_small),
                        contentDescription = null,
                        modifier = Modifier.size(55.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BCLab",
                        style = TextStyle(
                            color = Color(0xFF000000),
                            fontWeight = FontWeight.Bold,
                            fontSize = 45.sp
                        )
                    )
                }




                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Login Account",
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 20.dp)
                            .padding(top = 50.dp)
                            .padding(start = 30.dp),
                        style = TextStyle(
                            color = Color(0xFFD3D3D3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp
                        )
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_username),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .padding(start = 16.dp)
                            .padding(end = 16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(16.dp),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            containerColor = Color(0xFFF4F4F9)
//                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_password),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .padding(start = 16.dp)
                            .padding(end = 16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(16.dp),
//                        colors = TextFieldDefaults.outlinedTextFieldColors(
//                            containerColor = Color(0xFFF4F4F9)
//                        )

                    )

                    // Display an error message if login fails
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Button(
                        onClick = {

                            if (username.isNotBlank() && password.isNotBlank()) {
                                userViewModel.loginUser(username, password) { userId ->
                                    if (userId != null) {
                                        // Navigate to the create transaction screen, passing the userId
                                        navController.navigate("createTransaction/$userId")
                                    } else {
                                        errorMessage = "Invalid username or password"
                                    }
                                }
                            } else {
                                errorMessage = "Please enter both username and password"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(start = 16.dp)
                            .padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB8DBD9)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Text("Login")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            navController.navigate("viewCredentials")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .padding(end = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF515151)
                        )
                    ) {
                        Text("View Saved Credentials")
                    }
                }
            }
        }
    )
}


fun generateUniqueQRCodeString(): String {
    // Generate a unique string using UUID
    return UUID.randomUUID().toString()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(navController: NavHostController, userId: Int?, transactionViewModel: TransactionViewModel) {
    // Fetch the transactions for the user
    LaunchedEffect(userId) {
        transactionViewModel.getTransactionsForUser(userId)
    }

    // Observe the transactions LiveData
    val transactions by transactionViewModel.transactions.observeAsState(emptyList())

    // Define the corner radius and border width
    val cornerRadius = 12.dp
    val borderWidth = 2.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .clip(RoundedCornerShape(cornerRadius))    // Apply corner radius
            .border(
                width = borderWidth,
                color = Color.Black,                   // Border color
                shape = RoundedCornerShape(cornerRadius)  // Same corner radius for the border
            )
            .background(Color(0xFFC0C0C0))

    ) {
        Text(text = "Transaction List for User ID: $userId",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF515151),
            modifier = Modifier.padding(top = 30.dp, start = 16.dp))

        // Show list of transactions
        if (transactions.isNotEmpty()) {
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)) {
                items(transactions) { transaction ->
                    TransactionItem(transaction, transactionViewModel) {
                        // Navigate to TransactionDetailsScreen and pass transactionId and userId
                        navController.navigate("transactionDetails/${transaction.id}/$userId")
                    }
                }
            }
        } else {
            Text(text = "No transactions found for this user.")
        }
    }
}


@Composable
fun TransactionItem(transaction: Transaction, transactionViewModel: TransactionViewModel, onClick: () -> Unit) {
    // Define the corner radius and border width
    val cornerRadius = 12.dp
    val borderWidth = 2.dp

    // Determine the color based on the transaction's status
    val statusColor = when (transaction.status) {
        "Approved" -> Color(0xFF00A550)
        "Rejected" -> Color(0xFFED2939)
        else -> Color.Gray
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .clickable(onClick = onClick) // Handle click on transaction
//            .border(1.dp, Color.Gray)
            .padding(10.dp)
            .clip(RoundedCornerShape(cornerRadius))    // Apply corner radius
            .border(
                width = borderWidth,
                color = Color.Gray,                   // Border color
                shape = RoundedCornerShape(cornerRadius)  // Same corner radius for the border
            )

    ) {
        transactionViewModel.updateUserFinalApproval(transaction.id, transaction.firstUserApproved, transaction.secondUserApproved)

        Column(modifier = Modifier.padding(12.dp)) {  // Optional: Add padding to the entire column
            // Add top and bottom padding to each text
            Text(
                text = "Transaction ID: ${transaction.id}",
                modifier = Modifier.padding(vertical = 6.dp)  // Padding top and bottom
            )
            Text(
                text = "First User ID: ${transaction.firstUserId}",
                modifier = Modifier.padding(vertical = 6.dp)  // Padding top and bottom
            )
            Text(
                text = "Second User ID: ${transaction.secondUserId}",
                modifier = Modifier.padding(vertical = 6.dp)  // Padding top and bottom
            )
            Text(
                text = "Status: ${transaction.status}",
                color = statusColor,
                modifier = Modifier.padding(vertical = 6.dp)  // Padding top and bottom
            )
        }
    }
}

@Composable
fun TransactionDetailsScreen(
    navController: NavHostController,
    transactionId: Int,
    userId: Int,
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val context = LocalContext.current
    var scannedQRCode by remember { mutableStateOf("") }

    // Fetch the transaction details by ID
    LaunchedEffect(transactionId) {
        transactionViewModel.getTransactionById(transactionId)
    }

    // Observe the selected transaction
    val transaction by transactionViewModel.selectedTransaction.observeAsState()

    // QR Code scanner launcher
    val qrScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get the QR code content from the scan result
            val intent = result.data
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, intent)
            if (scanResult != null && scanResult.contents != null) {
                scannedQRCode = scanResult.contents // Save the scanned QR code
                // Check the scanned QR code and update transaction approval status
                transaction?.let { tx ->
                    when (scannedQRCode) {
                        tx.firstQRCode -> {
                            // Update the first user's approval
                            transactionViewModel.updateUserApproval(
                                tx.id,
                                firstUserApproved = true
                            )
                        }
                        tx.secondQRCode -> {
                            // Update the second user's approval
                            transactionViewModel.updateUserApproval(
                                tx.id,
                                secondUserApproved = true
                            )
                        }
                        else -> {
                            // The scanned QR code doesn't match either
                            Toast.makeText(context, "QR Code doesn't match!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                scannedQRCode = "Scan failed or canceled."
            }
        }
    }

    transaction?.let { tx ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .background(Color(0xFFB8DBD9)),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Transaction ID: ${tx.id}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),  // Set text to bold,
                color = Color(0xFF515151),
                modifier = Modifier.padding(top = 50.dp, start = 16.dp)
            )


            // If the status is not "Rejected", show the QR code section
            if (tx.status == "Pending") {
                // Determine which QR code to show based on userId
                val qrCodeString = when (userId) {
                    tx.firstUserId -> tx.firstQRCode
                    tx.secondUserId -> tx.secondQRCode
                    else -> ""
                }

                if (qrCodeString.isNotEmpty()) {
                    // Generate and display the QR code image
                    val qrCodeBitmap = remember { generateQRCode(qrCodeString, 500) }

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        bitmap = qrCodeBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 15.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(text = "QR Code not available for this user.")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to scan a QR code
                Button(
                    onClick = {
                        // Start camera to scan QR code
                        val integrator = IntentIntegrator(context as Activity)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        integrator.setPrompt("Scan a QR code")
                        integrator.setOrientationLocked(false)
                        qrScanLauncher.launch(integrator.createScanIntent())
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .padding(top = 15.dp), // Center the Button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC0C0C0), // Change button color
                        contentColor = Color.White           // Change text color
                    )
                ) {
                    Text(text = "Scan QR Code")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Display the scanned QR code result
//            if (scannedQRCode.isNotEmpty()) {
//                Text(text = "Scanned QR Code: $scannedQRCode")
//            }

//                Spacer(modifier = Modifier.height(16.dp))

//                // Approve Button
//                Button(
//                    onClick = {
//                        // Approve the transaction based on which user it is
//                        when (scannedQRCode) {
//                            tx.firstQRCode -> {
//                                transactionViewModel.updateUserApproval(
//                                    tx.id,
//                                    firstUserApproved = true
//                                )
//                            }
//
//                            tx.secondQRCode -> {
//                                transactionViewModel.updateUserApproval(
//                                    tx.id,
//                                    secondUserApproved = true
//                                )
//                            }
//
//                            else -> {
//                                Toast.makeText(
//                                    context,
//                                    "QR Code doesn't match!",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                    }
//                ) {
//                    Text(text = "Approve")
//                }
//
//                // Reject Button
//                Button(
//                    onClick = {
//                        // Reject the transaction
//                        transactionViewModel.rejectTransaction(tx.id)
//                    }
//                ) {
//                    Text(text = "Reject")
//                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Approve Button
                    Button(
                        onClick = {
                            // Approve the transaction based on which user it is
                            when (scannedQRCode) {
                                tx.firstQRCode -> {
                                    transactionViewModel.updateUserApproval(
                                        tx.id,
                                        firstUserApproved = true
                                    )
                                }

                                tx.secondQRCode -> {
                                    transactionViewModel.updateUserApproval(
                                        tx.id,
                                        secondUserApproved = true
                                    )
                                }

                                else -> {
                                    Toast.makeText(
                                        context,
                                        "QR Code doesn't match!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        // Optional: Adds space between buttons
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF50C878), // Change button color
                            contentColor = Color.White           // Change text color
                        )
                    ) {
                        Text(text = "Approve")
                    }

                    // Reject Button
                    Button(
                        onClick = {
                            // Reject the transaction
                            transactionViewModel.rejectTransaction(tx.id)
                        },
                        modifier = Modifier.width(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE52B50), // Change button color
                            contentColor = Color.White           // Change text color
                        )
                    ) {
                        Text(text = "Reject")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Show the current transaction status

            }
            Text(
                text = "Transaction Status: ${tx.status}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF747474),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    } ?: run {
        // Handle the case where the transaction is not found or still loading
        Text("Transaction not found or still loading.")
    }
}




fun generateQRCode(text: String, size: Int): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bmp
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTransactionScreen(navController: NavHostController, transactionId: String?) {
    val context = LocalContext.current
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            capturedImage = imageBitmap
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Transaction QR") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC0C0C0)
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFC0C0C0))
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Check camera permission
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Launch camera intent to capture image
                    val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(cameraIntent)
                } else {
                    // Request camera permission
                    androidx.core.app.ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(android.Manifest.permission.CAMERA),
                        100
                    )
                }
//                Button(
//                    onClick = {
//                        // Check camera permission
//                        if (ContextCompat.checkSelfPermission(
//                                context,
//                                android.Manifest.permission.CAMERA
//                            ) == PackageManager.PERMISSION_GRANTED
//                        ) {
//                            // Launch camera intent to capture image
//                            val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
//                            cameraLauncher.launch(cameraIntent)
//                        } else {
//                            // Request camera permission
//                            androidx.core.app.ActivityCompat.requestPermissions(
//                                context as Activity,
//                                arrayOf(android.Manifest.permission.CAMERA),
//                                100
//                            )
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 16.dp)
//                        .padding(start = 16.dp)
//                        .padding(end = 16.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFB8DBD9)
//                    )
//                ) {
//                    Text("Scan QR Code (Open Camera)")
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))

                // Display the captured image if available
                capturedImage?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .size(300.dp)
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    )
}

fun generateTransactionId(): String {
    return java.util.UUID.randomUUID().toString()
}

fun sendTransactionConfirmationToServer(context: Context, transactionId: String) {
    // اینجا درخواست به سرور ارسال می‌شود
    Toast.makeText(context, "Transaction confirmed for $transactionId", Toast.LENGTH_LONG).show()

    // پس از ارسال موفقیت‌آمیز به سرور، می‌توانیم یک Notification ارسال کنیم
    showTransactionSuccessNotification(context)
}

fun showTransactionSuccessNotification(context: Context) {
    // پیامی که به کاربران ارسال خواهد شد اگر تراکنش موفقیت آمیز بود
    Toast.makeText(context, "Transaction successful! Notification sent.", Toast.LENGTH_LONG).show()
}

@Composable
fun QRCodeImage(data: String) {
    val bitmap = remember(data) { generateQRCode(data) }
    bitmap?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(128.dp))
    }
}

fun generateQRCode(data: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

