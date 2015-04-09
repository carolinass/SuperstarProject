#install.packages('RPostgreSQL')
#install.packages('ggplot2')
library('ggplot2')
library('RPostgreSQL')

## loads the PostgreSQL driver
drv <- dbDriver("PostgreSQL")

## Open a connection
con <- dbConnect(drv, host="192.168.0.119", port="5432", user="postgres", password="hackfest", dbname="superstars")

## Submits a statement
rs <- dbSendQuery(con, "CREATE TABLE tweetsSentimento (
  id             SERIAL PRIMARY KEY,
  id_tweet       TEXT UNIQUE NOT NULL,
  text           TEXT NOT NULL,
  created        DATE NOT NULL,
  artist         TEXT NOT NULL,
  longitude      TEXT,
  latitude       TEXT,
  sentimento     INT DEFAULT 0
)")


dados.malta <- dbGetQuery(con, "SELECT created, count(*) FROM tweets WHERE artist='malta' AND sentimento=1 GROUP BY created ORDER BY created")
dados.malta$week <- as.integer(difftime(strptime(dados.malta$created, "%Y-%m-%d"), strptime('2014-01-01', "%Y-%m-%d"), unit="week"))
dados.malta <- aggregate(count ~ week, dados.malta, sum)

dados.luan <- dbGetQuery(con, "SELECT created, count(*) FROM tweets WHERE artist='luan' AND sentimento=1  GROUP BY created ORDER BY created")
dados.luan$week <- as.integer(difftime(strptime(dados.luan$created, "%Y-%m-%d"), strptime('2014-01-01', "%Y-%m-%d"), unit="week"))
dados.luan <- aggregate(count ~ week, dados.luan, sum)

dados.jamz <- dbGetQuery(con, "SELECT created, count(*) FROM tweets WHERE artist='jamz' AND sentimento=1  GROUP BY created ORDER BY created")
dados.jamz$week <- as.integer(difftime(strptime(dados.jamz$created, "%Y-%m-%d"), strptime('2014-01-01', "%Y-%m-%d"), unit="week"))
dados.jamz <- aggregate(count ~ week, dados.jamz, sum)

dados.suricato <- dbGetQuery(con, "SELECT created, count(*) FROM tweets WHERE artist='suricato' AND sentimento=1  GROUP BY created ORDER BY created")
dados.suricato$week <- as.integer(difftime(strptime(dados.suricato$created, "%Y-%m-%d"), strptime('2014-01-01', "%Y-%m-%d"), unit="week"))
dados.suricato <- aggregate(count ~ week, dados.suricato, sum)

rects <- data.frame(xstart = c(26,14,-Inf), xend = c(Inf, 26, 14), col = c("Pós Superstars", "Durante Supestars", "Pré Superstars"))

ggplot() +
  geom_rect(data = rects, aes(xmin = xstart, xmax = xend, ymin = -Inf, ymax = Inf, fill = col), alpha = 0.5) +
  scale_fill_manual(name = "Período", values = c('#fc8d59', '#91cf60', '#ffffbf')) +
  geom_line(data = dados.luan, aes(x = week, y = count, colour="Luan", linetype="Luan"), size = 1, ) +
  geom_line(data = dados.malta, aes(x = week, y = count, colour="Malta", linetype="Malta"), size = 1) +
  geom_line(data = dados.jamz, aes(x = week, y = count, colour="Jamz", linetype="Jamz"), size = 1) +
  geom_line(data = dados.suricato, aes(x = week, y = count, colour="Suricato", linetype="Suricato"), size = 1) +
  scale_color_manual(name = "Artistas", values = c('#e41a1c','#377eb8','#4daf4a','#ff7f00')) +
  scale_linetype_manual(values=c(1,1,1,1), guide = FALSE) +
  ggtitle("Tweets sobre os Artistas") +
  xlab("Semana do Ano") +
  ylab("Quantidade de tweets") +
  theme_bw() +
  theme(plot.title = element_text(lineheight=.8, face="bold"))

ggplot() +
  geom_rect(data = rects, aes(xmin = xstart, xmax = xend, ymin = -Inf, ymax = Inf, fill = col), alpha = 0.5) +
  scale_fill_manual(name = "Período", values = c('#fc8d59', '#91cf60', '#ffffbf')) +
  geom_line(data = dados.jamz, aes(x = week, y = count), size = 1, ) +
  ggtitle("Tweets com Sentimento sobre Jamz") +
  xlab("Semana do Ano") +
  ylab("Quantidade de tweets") +
  theme_bw() +
  theme(plot.title = element_text(lineheight=.8, face="bold"))


dados.all <- dbGetQuery(con, "SELECT artist, count(*) AS count
                         FROM tweets
                         WHERE sentimento = 1
                         GROUP BY artist
                         ORDER BY count DESC")

