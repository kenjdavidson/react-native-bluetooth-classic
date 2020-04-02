export default Logger = (msg, data) => {
    console.log(msg);
    
    if (!!data)console.log(data);
}