const mineflayer = require("mineflayer");

const botCount = 2;

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

for (let i = 0; i < botCount; i++) {
    const bot = mineflayer.createBot({
        host: "127.0.0.1",
        port: 25565,
        username: `TestBot${i + 1}`,
        maxCatchupTicks: Number.MAX_SAFE_INTEGER,

    });

    console.log('Bot TestBot' + (i + 1) + ' created');

    bot.on('kicked', console.log)
    bot.on('error', console.log)

    bot._client.on('chat', (chat) => {
        console.log(`${bot.username} received chat: ${chat.message}`);
    })

    bot.on('spawn', async ()=>{
        //await wait(1000);

        setInterval(async() => {
            const block = bot.findBlock({matching:(b)=>true, maxDistance:5});
            // console.log('block found:', block);
            //bot.inte
            await bot.dig(block);
            await bot.stopDigging();
            console.log('interacted with a block');
            }, (i+1)*1000)

    })
}