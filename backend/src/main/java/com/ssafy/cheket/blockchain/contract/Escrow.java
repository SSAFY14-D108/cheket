package com.ssafy.cheket.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>
 * Auto generated code.
 * <p>
 * <strong>Do not modify!</strong>
 * <p>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j
 * command line tools</a>, or the
 * org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a href=
 * "https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen
 * module</a> to update.
 *
 * <p>
 * Generated with web3j version 4.12.3.
 */
@SuppressWarnings("rawtypes")
public class Escrow extends Contract {
    public static final String BINARY = "0x60803461015457601f61115738819003918201601f19168301916001600160401b0383118484101761015957808492604094855283398101031261015457610052602061004b8361016f565b920161016f565b60008054336001600160a01b0319821681178355604051949290916001600160a01b0316907f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09080a36001600160a01b031691821561012457506001600160a01b03169081156100eb5760018060a01b0319600154161760015560018060a01b03196002541617600255604051610fd390816101848239f35b60405162461bcd60e51b8152602060048201526011602482015270125b9d985b1a5908151a58dad95d139195607a1b6044820152606490fd5b62461bcd60e51b815260206004820152600b60248201526a24b73b30b634b21029a9a360a91b6044820152606490fd5b600080fd5b634e487b7160e01b600052604160045260246000fd5b51906001600160a01b03821682036101545756fe608080604052600436101561001357600080fd5b600090813560e01c90816303988f8414610d7e5750806331ea1a3914610c67578063715018a614610c0d57806374b4c53614610710578063812acf131461081857806382fd5bac146107ae5780638da5cb5b14610787578063966d1f1d14610769578063a4d320cc1461073a578063a81d5b6f14610710578063b393391b146106e7578063cdb6aa3614610347578063dde5b0de146101b7578063f2fde38b146100f15763fb74dabc146100c657600080fd5b346100ee57806003193601126100ee576001546040516001600160a01b039091168152602090f35b80fd5b50346100ee5760203660031901126100ee5761010b610e41565b610113610f45565b6001600160a01b031680156101635781546001600160a01b03198116821783556001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08380a380f35b60405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608490fd5b50346100ee5760203660031901126100ee576004356101d4610f45565b8082526004602052604082206006810160ff8154166004811015610333576101fc9015610e5c565b60058201544211156102fc57600360ff1982541617905560028101918254845260066020526040842060ff1981541690558360018060a01b036002541660018060a01b038454168554823b156102ed576040516323b872dd60e01b81523060048201526001600160a01b0392909216602483015260448201529082908290606490829084905af180156102f1576102d8575b50506001820154915492546040519081526001600160a01b0393841693909216917f1b2aa20e5b03b93ea6a21155a0f4eada25927b7a22cb029fe1fb842ab09cca9690602090a480f35b816102e291610e9d565b6102ed57833861028e565b8380fd5b6040513d84823e3d90fd5b60405162461bcd60e51b815260206004820152600f60248201526e139bdd08195e1c1a5c9959081e595d608a1b6044820152606490fd5b634e487b7160e01b85526021600452602485fd5b50346100ee5760c03660031901126100ee57610361610e41565b60243560443591606435926084359160a4359061037c610f45565b6001600160a01b0381169384156106b157858852600660205260ff60408920541661067957831561064057428311156105fb578087029087820414871517156105e757612710900483116105ad576002548791906001600160a01b0316803b156105a9576040516323b872dd60e01b81526001600160a01b039290921660048301523060248301526044820187905282908290606490829084905af180156102f157610590575b505060035494600019861461057c57600186016003556040519060e0820182811067ffffffffffffffff82111761056857604090815285835260208084018a8152828501898152606086018881526080870195865260a0870188815260c088018e81528d8f52600495869052958e20975188546001600160a01b03199081166001600160a01b03928316178a55945160018a018054909616911617909355905160028701555160038601559251848201559151600584015551600692909201919081101561055457927fdbed7d56dc9bc6aeb14df6b8c0becfa98acd06d60cc5200f0ca43bc73cc46ae892604060209981948a9760ff8019835416911617905588815260058b52868282205588815260068b5220600160ff19825416179055825191825288820152a4604051908152f35b634e487b7160e01b88526021600452602488fd5b634e487b7160e01b89526041600452602489fd5b634e487b7160e01b87526011600452602487fd5b8161059a91610e9d565b6105a5578538610423565b8580fd5b8280fd5b60405162461bcd60e51b815260206004820152601260248201527104578636565647320726573616c65206361760741b6044820152606490fd5b634e487b7160e01b88526011600452602488fd5b60405162461bcd60e51b815260206004820152601760248201527f446561646c696e65206d757374206265206675747572650000000000000000006044820152606490fd5b60405162461bcd60e51b815260206004820152601160248201527005072696365206d757374206265203e203607c1b6044820152606490fd5b60405162461bcd60e51b815260206004820152601060248201526f105b1c9958591e48195cd8dc9bddd95960821b6044820152606490fd5b60405162461bcd60e51b815260206004820152600e60248201526d24b73b30b634b21039b2b63632b960911b6044820152606490fd5b50346100ee57806003193601126100ee576002546040516001600160a01b039091168152602090f35b50346100ee5760203660031901126100ee5760406020916004358152600583522054604051908152f35b50346100ee5760203660031901126100ee5760ff60406020926004358152600684522054166040519015158152f35b50346100ee57806003193601126100ee576020600354604051908152f35b50346100ee57806003193601126100ee57546040516001600160a01b039091168152602090f35b50346100ee5760203660031901126100ee57604090600435815260046020522060018060a01b0381541661081460018060a01b0360018401541692600281015490600381015460048201549060ff60066005850154940154169360405197889788610de4565b0390f35b50346100ee5760403660031901126100ee57610832610e41565b6024359061083e610f45565b81835260046020526040832091600683019160ff8354166004811015610bf9576108689015610e5c565b83546001600160a01b038281169491168414610bbc5760058501544211610b8857600185810180546001600160a01b03191686179055815460ff1990811682179092556002860180548852600660209081526040808a2080549095169094559154875460038901805495516323b872dd60e01b81526001600160a01b03888116600483015292831660248201526044810196909652929892959294929391839160649183918d91165af1908115610b7d578891610b3e575b5015610b0357869060018060a01b0360025416908754823b156102ed576040516323b872dd60e01b81523060048201526001600160a01b0392909216602483015260448201529082908290606490829084905af180156102f157610aee575b5060018060a01b036002541686546040519063285a238960e11b825260048201528281602481855afa8015610ae35783918491610a61575b5084546001600160a01b031692803b15610a5d578492836084926040519687958694634db9bbfd60e11b86526004860152602485015260448401528b60648401525af180156102f157610a48575b50507f38d1d351aaed4d042e96c9e1ebf794cafeb9ec533318564d969b5335b2d2c3219160409160018060a01b039054169554905482519182526020820152a480f35b81610a5291610e9d565b6105a5578538610a05565b8480fd5b9150503d8084833e610a738183610e9d565b8101610120828203126102ed57815190602083015192604081015167ffffffffffffffff8111610adf5782610aa9918301610ed5565b5060a081015167ffffffffffffffff8111610adf5760e092610acc918301610ed5565b50015160ff8116036102ed5790386109b7565b8680fd5b6040513d85823e3d90fd5b81610af891610e9d565b6105a557853861097f565b60405162461bcd60e51b815260206004820152601360248201527214d4d1881d1c985b9cd9995c8819985a5b1959606a1b6044820152606490fd5b90506020813d602011610b75575b81610b5960209383610e9d565b81010312610b7157518015158103610b715738610920565b8780fd5b3d9150610b4c565b6040513d8a823e3d90fd5b60405162461bcd60e51b815260206004820152600c60248201526b1119585b08195e1c1a5c995960a21b6044820152606490fd5b60405162461bcd60e51b815260206004820152601560248201527410d85b9b9bdd08189d5e481bdddb881d1a58dad95d605a1b6044820152606490fd5b634e487b7160e01b86526021600452602486fd5b50346100ee57806003193601126100ee57610c26610f45565b80546001600160a01b03198116825581906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a380f35b50346100ee5760203660031901126100ee57600435610c84610f45565b8082526004602052604082206006810160ff815416600481101561033357610cac9015610e5c565b600260ff1982541617905560028101908154845260066020526040842060ff1981541690558360018060a01b03600254169160018060a01b0390541691835490803b156105a9576040516323b872dd60e01b81523060048201526001600160a01b0394909416602485015260448401919091528290606490829084905af18015610d7357610d5f575b5054907faf3855a84ba7ae9060a15c82675adab08caab3cb5ba10b102c3f0dd8279da0218380a380f35b83610d6c91949294610e9d565b9138610d35565b6040513d86823e3d90fd5b905034610de0576020366003190112610de05761081460408293600435815260046020522060018060a01b038154169060018060a01b03600182015416600282015460038301549060048401549260ff60066005870154960154169588610de4565b5080fd5b949290979695939160e086019860018060a01b0316865260018060a01b0316602086015260408501526060840152608083015260a08201526004821015610e2b5760c00152565b634e487b7160e01b600052602160045260246000fd5b600435906001600160a01b0382168203610e5757565b600080fd5b15610e6357565b60405162461bcd60e51b81526020600482015260126024820152712737ba1030bbb0b4ba34b73390313abcb2b960711b6044820152606490fd5b90601f8019910116810190811067ffffffffffffffff821117610ebf57604052565b634e487b7160e01b600052604160045260246000fd5b81601f82011215610e575780519067ffffffffffffffff8211610ebf5760405192610f0a601f8401601f191660200185610e9d565b82845260208383010111610e575760005b828110610f3057505060206000918301015290565b80602080928401015182828701015201610f1b565b6000546001600160a01b03163303610f5957565b606460405162461bcd60e51b815260206004820152602060248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152fdfea264697066735822122074b3b4a56f66410cf557d4ddd8edd2440a4d27820d1468314392b0255c52cec964736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_BUYANDSETTLE = "buyAndSettle";

    public static final String FUNC_CANCELDEAL = "cancelDeal";

    public static final String FUNC_CREATEDEAL = "createDeal";

    public static final String FUNC_DEALS = "deals";

    public static final String FUNC_GETDEAL = "getDeal";

    public static final String FUNC_GETDEALBYTICKET = "getDealByTicket";

    public static final String FUNC_ISESCROWED = "isEscrowed";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_REFUNDEXPIREDDEAL = "refundExpiredDeal";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SSFTOKEN = "ssfToken";

    public static final String FUNC_TICKETDEAL = "ticketDeal";

    public static final String FUNC_TICKETNFT = "ticketNFT";

    public static final String FUNC_TOTALDEALS = "totalDeals";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event DEALCANCELLED_EVENT = new Event("DealCancelled",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>(true) {
        }));;

    public static final Event DEALCREATED_EVENT = new Event("DealCreated",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event DEALEXPIREDREFUND_EVENT = new Event("DealExpiredRefund",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event DEALSETTLED_EVENT = new Event("DealSettled",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    @Deprecated
    protected Escrow(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Escrow(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Escrow(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Escrow(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DealCancelledEventResponse> getDealCancelledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALCANCELLED_EVENT,
            transactionReceipt);
        ArrayList<DealCancelledEventResponse> responses = new ArrayList<DealCancelledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealCancelledEventResponse typedResponse = new DealCancelledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealCancelledEventResponse getDealCancelledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALCANCELLED_EVENT, log);
        DealCancelledEventResponse typedResponse = new DealCancelledEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DealCancelledEventResponse> dealCancelledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealCancelledEventFromLog(log));
    }

    public Flowable<DealCancelledEventResponse> dealCancelledEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALCANCELLED_EVENT));
        return dealCancelledEventFlowable(filter);
    }

    public static List<DealCreatedEventResponse> getDealCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALCREATED_EVENT,
            transactionReceipt);
        ArrayList<DealCreatedEventResponse> responses = new ArrayList<DealCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealCreatedEventResponse typedResponse = new DealCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealCreatedEventResponse getDealCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALCREATED_EVENT, log);
        DealCreatedEventResponse typedResponse = new DealCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DealCreatedEventResponse> dealCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealCreatedEventFromLog(log));
    }

    public Flowable<DealCreatedEventResponse> dealCreatedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALCREATED_EVENT));
        return dealCreatedEventFlowable(filter);
    }

    public static List<DealExpiredRefundEventResponse> getDealExpiredRefundEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALEXPIREDREFUND_EVENT,
            transactionReceipt);
        ArrayList<DealExpiredRefundEventResponse> responses = new ArrayList<DealExpiredRefundEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealExpiredRefundEventResponse typedResponse = new DealExpiredRefundEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealExpiredRefundEventResponse getDealExpiredRefundEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALEXPIREDREFUND_EVENT, log);
        DealExpiredRefundEventResponse typedResponse = new DealExpiredRefundEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<DealExpiredRefundEventResponse> dealExpiredRefundEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealExpiredRefundEventFromLog(log));
    }

    public Flowable<DealExpiredRefundEventResponse> dealExpiredRefundEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALEXPIREDREFUND_EVENT));
        return dealExpiredRefundEventFlowable(filter);
    }

    public static List<DealSettledEventResponse> getDealSettledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DEALSETTLED_EVENT,
            transactionReceipt);
        ArrayList<DealSettledEventResponse> responses = new ArrayList<DealSettledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DealSettledEventResponse typedResponse = new DealSettledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DealSettledEventResponse getDealSettledEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DEALSETTLED_EVENT, log);
        DealSettledEventResponse typedResponse = new DealSettledEventResponse();
        typedResponse.log = log;
        typedResponse.dealId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.ticketId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ssfAmount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<DealSettledEventResponse> dealSettledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDealSettledEventFromLog(log));
    }

    public Flowable<DealSettledEventResponse> dealSettledEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DEALSETTLED_EVENT));
        return dealSettledEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT,
            transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
        DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> buyAndSettle(String buyer, BigInteger dealId) {
        final Function function = new Function(FUNC_BUYANDSETTLE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, buyer),
                new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> cancelDeal(BigInteger dealId) {
        final Function function = new Function(FUNC_CANCELDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createDeal(String seller, BigInteger ticketId, BigInteger ssfAmount,
        BigInteger originalPrice, BigInteger resaleCapBps, BigInteger deadline) {
        final Function function = new Function(FUNC_CREATEDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, seller),
                new org.web3j.abi.datatypes.generated.Uint256(ticketId),
                new org.web3j.abi.datatypes.generated.Uint256(ssfAmount),
                new org.web3j.abi.datatypes.generated.Uint256(originalPrice),
                new org.web3j.abi.datatypes.generated.Uint256(resaleCapBps),
                new org.web3j.abi.datatypes.generated.Uint256(deadline)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> deals(
        BigInteger param0) {
        final Function function = new Function(FUNC_DEALS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint8>() {
            }));
        return new RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(
            function,
            new Callable<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                @Override
                public Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call()
                    throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                        (String) results.get(0).getValue(), (String) results.get(1).getValue(),
                        (BigInteger) results.get(2).getValue(), (BigInteger) results.get(3).getValue(),
                        (BigInteger) results.get(4).getValue(), (BigInteger) results.get(5).getValue(),
                        (BigInteger) results.get(6).getValue());
                }
            });
    }

    public RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> getDeal(
        BigInteger dealId) {
        final Function function = new Function(FUNC_GETDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Address>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint8>() {
            }));
        return new RemoteFunctionCall<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(
            function,
            new Callable<Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                @Override
                public Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call()
                    throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple7<String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                        (String) results.get(0).getValue(), (String) results.get(1).getValue(),
                        (BigInteger) results.get(2).getValue(), (BigInteger) results.get(3).getValue(),
                        (BigInteger) results.get(4).getValue(), (BigInteger) results.get(5).getValue(),
                        (BigInteger) results.get(6).getValue());
                }
            });
    }

    public RemoteFunctionCall<BigInteger> getDealByTicket(BigInteger ticketId) {
        final Function function = new Function(FUNC_GETDEALBYTICKET,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(ticketId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Boolean> isEscrowed(BigInteger param0) {
        final Function function = new Function(FUNC_ISESCROWED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> refundExpiredDeal(BigInteger dealId) {
        final Function function = new Function(FUNC_REFUNDEXPIREDDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(dealId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> ssfToken() {
        final Function function = new Function(FUNC_SSFTOKEN, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> ticketDeal(BigInteger param0) {
        final Function function = new Function(FUNC_TICKETDEAL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> ticketNFT() {
        final Function function = new Function(FUNC_TICKETNFT, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalDeals() {
        final Function function = new Function(FUNC_TOTALDEALS, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Escrow load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new Escrow(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Escrow load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new Escrow(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Escrow load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new Escrow(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Escrow load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new Escrow(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Escrow> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    public static RemoteCall<Escrow> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Escrow> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Escrow> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice,
        BigInteger gasLimit, String _ssfToken, String _ticketNFT) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(
            new org.web3j.abi.datatypes.Address(160, _ssfToken), new org.web3j.abi.datatypes.Address(160, _ticketNFT)));
        return deployRemoteCall(Escrow.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(),
            encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class DealCancelledEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public BigInteger ticketId;
    }

    public static class DealCreatedEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public String seller;

        public BigInteger ticketId;

        public BigInteger ssfAmount;

        public BigInteger deadline;
    }

    public static class DealExpiredRefundEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public String buyer;

        public String seller;

        public BigInteger ticketId;
    }

    public static class DealSettledEventResponse extends BaseEventResponse {
        public BigInteger dealId;

        public String buyer;

        public String seller;

        public BigInteger ticketId;

        public BigInteger ssfAmount;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }
}
