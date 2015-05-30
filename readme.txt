___.   .__                   __ ________
\_ |__ |  | _____    _______/  |\_____  \______
 | __ \|  | \__  \  /  ___/\   __\_(__  <_  __ \
 | \_\ \  |__/ __ \_\___ \  |  | /       \  | \/
 |___  /____(____  /____  > |__|/______  /__|
     \/          \/     \/             \/
version 0
	(C) 2015 Andrew Breksa [abreksa4@gmail.com]

blast3r uses various open source technologies and data sources, including:
	ttorrent   (http://mpetazzoni.github.io/ttorrent/),
	Strike API (https://getstrike.net/api/)
and requires nmap for some optional functionality (https://nmap.org/).
See the "--disable-nmap" option.

Options:

 VAL                                    : The names of the target files to load
 --data-directory (-datad) VAL          : The directory that holds the data
                                          files (default: data/)
 --disable-nmap                         : disables using nmap to scan for peers
                                          (default: false)
 --download-directory (-downd) VAL      : The directory that holds the
                                          downloaded files (default: downloads/)
 --help (-h, --?, -?)                   : Display this help text and exit
                                          (default: false)
 --info                                 : diaplay extra information in the help
                                          output (default: false)
 --log-file VAL                         : The log file (default: log.log)
 --log-level (-ll) [NONE | ERROR |      : The log level (default: INFO)
 WARN | INFO | DEBUG | TRACE]
 --log-to-file (-l2f)                   : If blast3r should log to a file
                                          (default: false)
 --nmap-coomand-line VAL                : The nmap command line to use to
                                          discover peers (default: nmap
                                          --script bittorrent-discovery
                                          --script-args 'bittorrent-discovery.ma
                                          gnet="%s"' | grep -E -o
                                          "([0-9]{1,3}[\.]){3}[0-9]{1,3}")
 --peers (--get-peers)                  : Get the current peers of each torrent
                                          (default: false)
 --save-config                          : Saves the provided config to disk
                                          (default: false)
 --strike-download-url VAL              : The strike download url (default:
                                          https://getstrike.net/torrents/api/dow
                                          nload/%s.torrent)
 --target-directory (-td) VAL           : The directory which holds the target
                                          files (default: targets/)
 --torrage-url VAL                      : The torrage url (default:
                                          http://torrage.info/download.php?h=%s)
 --ttorrent-sleep (-ts) N               : The time in seconds to sleep to wait
                                          for peers (default: 30000)
 --ttorrent-sleep-count (-tsc) N        : The number of times to let ttorrent
                                          sleep to find peers before moving on
                                          (default: 3)
 --ttorrent-sleep-peer-count (-tspc) N  : The minimum nuber of peers for
                                          ttorrent to have before not sleeping.
                                          (default: 1)
 --user-agent VAL                       : The user agent to use while accessing
                                          strike and torrage (default:
                                          Mozilla/5.0 (Macintosh; U; Intel Mac
                                          OS X 10.4; en-US; rv:1.9.2.2)
                                          Gecko/20100316 Firefox/3.6.2)