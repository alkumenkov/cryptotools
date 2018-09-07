# cryptotools
Trading terminal and logging system for various exchanges (here realized binance and okex)

<p>Overview</p>
Theese utilities provide you to control your algorithmic trading. Uses <a href='https://github.com/alkumenkov/accounting_database'>this database</a>. 
Contsist of 3 apps:

1.DesktopClient - trading terminal for each account. Shows history of deals and offers and able to create orders. 
Quantitative systems, especially while being in debugging stage, usually crashes. Unfortunately, they can leave enourmous position, 
which must be covered immediately. DesktopClient handles account's trading events and position changes in realtime and able to make trades, so, it useful for such situations.

2.UpdatingDBase - utilities, which save into <a href='https://github.com/alkumenkov/accounting_database'>database</a> all tradings, offers, positions for each account. This information uses for further accounting and quantative analyzing. Also, UpdatingDBase saves snapshots of prices for all instruments. This uses for making portfolio evaluation or, for example, for analyzing positions. Each exchange has each couple of tables for importing all this information. UpdatingDBase provides to save it for all this couples.

3.TInstrumentDBaseFixer - automaticly saves information about new coin into <a href='https://github.com/alkumenkov/accounting_database'>database</a>. There are more, than 100 instruments trades on exchanges. It will be hardly difficult to control, which of them are currently used by your quantitative system or which of them were bought because of error or bug happens. If there is a new position of coin, which never been stored, this utility registers information about this instrument. After registration, DesktopClient and UpdatingDBase will be able to work with it.
