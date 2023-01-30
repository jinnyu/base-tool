/*
 * Copyright (c) 2022, Jinnyu (jinyu@jinnyu.cn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.jinnyu.base.mail;

import cn.jinnyu.base.lang.LangKit;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author jinyu@jinnyu.cn
 * @date 2023-01-30
 */
public enum MailKit {

    SMTP, EXCHANGE;

    public void send(MailConfig config) throws Exception {
        if (SMTP.equals(this)) {
            SMTP smtp = new SMTP();
            smtp.sendMail(config);
        } else {
            Exchange exchange = new Exchange();
            exchange.sendMail(config);
        }
    }

    @Slf4j
    private static class SMTP {

        public void sendMail(MailConfig config) throws Exception {
            Properties  properties = configSMTP(config);
            Session     s          = getSmtpSession(properties, config.getUsername(), config.getPassword());
            MimeMessage message    = getSmtpMessage(s, config);
            Transport   transport  = getSmtpTransport(s, config);
            doSmtpSend(transport, message);
        }

        private Properties configSMTP(MailConfig m) {
            Properties properties = new Properties();
            if (m.getSsl()) {
                properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.setProperty("mail.smtp.socketFactory.fallback", "false");
                properties.setProperty("mail.smtp.port", m.getPort());
                properties.setProperty("mail.smtp.socketFactory.port", m.getPort());
            }
            properties.setProperty("mail.smtp.host", m.getHost());
            properties.put("mail.smtp.auth", "true");
            return properties;
        }

        private Session getSmtpSession(Properties props, String username, String password) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }

        private MimeMessage getSmtpMessage(Session s, MailConfig m) throws MessagingException {
            MimeMessage message = new MimeMessage(s);
            // 发件人
            message.setFrom(new InternetAddress(m.getFrom()));
            // 收件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(m.getTo(), false));
            // 抄送
            if (!LangKit.isEmpty(m.getCc())) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(m.getCc(), false));
            }
            // 秘密抄送
            if (!LangKit.isEmpty(m.getBcc())) {
                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(m.getBcc(), false));
            }
            // 主题
            message.setSubject(m.getSubject(), StandardCharsets.UTF_8.name());
            // 内容
            ContentType ct     = m.getContentType();
            String      ctName = null;
            switch (ct) {
                case TEXT_PLAIN:
                case TEXT_HTML:
                case TEXT_XML:
                    ctName = ct.name().toLowerCase().replace("_", "/");
                    break;
                case MULTIPART:
                    ctName = ct.name().toLowerCase() + "/*";
                    break;
                case RFC822:
                    ctName = "message/" + ct.name().toLowerCase();
                    break;
                default:
                    break;
            }
            message.setContent(m.getContent(), ctName + "; charset=UTF-8");
            // 发送时间
            message.setSentDate(new Date());
            message.saveChanges();
            return message;
        }

        private Transport getSmtpTransport(Session s, MailConfig m) throws MessagingException {
            Transport transport = s.getTransport("smtp");
            transport.connect(m.getHost(), m.getUsername(), m.getPassword());
            return transport;
        }

        private void doSmtpSend(Transport transport, Message message) throws MessagingException {
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }

    }

    @Slf4j
    private static class Exchange {

        public void sendMail(MailConfig m) throws Exception {
            try {
                ExchangeService service = configExchange(m);
                EmailMessage    msg     = getExchangeMessage(service, m);
                doExchangeSend(msg);
            } catch (Error e) {
                if ("java.lang.NoClassDefFoundError".equals(e.getClass().getName())) {
                    System.err.println("Exchange need Apache HttpClient jar!");
                }
                throw e;
            }
        }

        private ExchangeService configExchange(MailConfig m) throws Exception {
            ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2007_SP1);
            service.setCredentials(new WebCredentials(m.getUsername(), m.getPassword()));
            service.setUrl(new URI("https://" + m.getHost() + "/ews/Exchange.asmx"));
            return service;
        }

        private EmailMessage getExchangeMessage(ExchangeService service, MailConfig m) throws Exception {
            EmailMessage msg = new EmailMessage(service);
            msg.setSubject(m.getSubject());
            msg.setBody(getExchangeBody(m));
            Stream.of(m.getTo().split(",")).forEach(address -> {
                try {
                    msg.getToRecipients().add(address);
                } catch (ServiceLocalException e) {
                    throw new RuntimeException(e);
                }
            });
            // 抄送
            if (!LangKit.isEmpty(m.getCc())) {
                Stream.of(m.getCc().split(",")).forEach(address -> {
                    try {
                        msg.getCcRecipients().add(address);
                    } catch (ServiceLocalException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            // 秘密抄送
            if (!LangKit.isEmpty(m.getBcc())) {
                Stream.of(m.getBcc().split(",")).forEach(address -> {
                    try {
                        msg.getBccRecipients().add(address);
                    } catch (ServiceLocalException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            return msg;
        }

        private MessageBody getExchangeBody(MailConfig m) {
            ContentType type = m.getContentType();
            MessageBody body;
            switch (type) {
                case TEXT_PLAIN:
                    body = new MessageBody(BodyType.Text, (String) m.getContent());
                    break;
                case TEXT_HTML:
                    body = new MessageBody(BodyType.HTML, (String) m.getContent());
                    break;
                default:
                    throw new RuntimeException("Exchange does not support this type content body!");
            }
            return body;
        }

        private void doExchangeSend(EmailMessage msg) throws Exception {
            msg.send();
        }

    }

    @Data
    public static class MailConfig {
        /**
         * SMTP地址 / Exchange地址(不包含 https:// 和 /ews/Exchange.asmx)
         */
        private String      host;
        /**
         * SMTP端口 (仅SMTP需配置)
         */
        private String      port        = "25";
        /**
         * 仅SMTP需配置
         */
        private Boolean     ssl         = false;
        /**
         * 仅SMTP需配置
         */
        private String      from;
        /**
         * 收件人 (多个目标时英文逗号分隔)
         */
        private String      to;
        /**
         * 抄送 (多个目标时英文逗号分隔)
         */
        private String      cc;
        /**
         * 秘密抄送 (多个目标时英文逗号分隔)
         */
        private String      bcc;
        /**
         * 邮件主题
         */
        private String      subject;
        /**
         * 发送账户
         */
        private String      username;
        /**
         * 发送密码
         */
        private String      password;
        /**
         * 发送内容<br> 注意: Exchange模式下只支持文本类型邮件
         */
        private Object      content;
        /**
         * 发送类型<br> 注意: Exchange模式下只支持文本类型邮件
         */
        private ContentType contentType = ContentType.TEXT_PLAIN;
    }

    public enum ContentType {
        /**
         * 纯文本
         */
        TEXT_PLAIN,
        /**
         * html
         */
        TEXT_HTML,
        /**
         * xml
         */
        TEXT_XML,
        /**
         * 带附件
         */
        MULTIPART,
        /**
         * RFC822
         */
        RFC822
    }

}
