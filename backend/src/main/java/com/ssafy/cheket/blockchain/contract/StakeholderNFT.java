package com.ssafy.cheket.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
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
public class StakeholderNFT extends Contract {
    public static final String BINARY = "0x60806040523461037357604080519081016001600160401b03811182821017610280576040908152601282527121a422a5a2aa1029ba30b5b2b437b63232b960711b602083015280519081016001600160401b038111828210176102805760405260048152634353544b60e01b602082015281516001600160401b03811161028057600054600181811c91168015610369575b602082101461026057601f8111610305575b50602092601f82116001146102a15792819293600092610296575b50508160011b916000199060031b1c1916176000555b80516001600160401b03811161028057600154600181811c91168015610276575b602082101461026057601f81116101fb575b50602091601f82116001146101975791819260009261018c575b50508160011b916000199060031b1c1916176001555b60068054336001600160a01b03198216811790925560405191906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0600080a361199490816103798239f35b015190503880610122565b601f198216926001600052806000209160005b8581106101e3575083600195106101ca575b505050811b01600155610138565b015160001960f88460031b161c191690553880806101bc565b919260206001819286850151815501940192016101aa565b60016000527fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf6601f830160051c81019160208410610256575b601f0160051c01905b81811061024a5750610108565b6000815560010161023d565b9091508190610234565b634e487b7160e01b600052602260045260246000fd5b90607f16906100f6565b634e487b7160e01b600052604160045260246000fd5b0151905038806100bf565b601f1982169360008052806000209160005b8681106102ed57508360019596106102d4575b505050811b016000556100d5565b015160001960f88460031b161c191690553880806102c6565b919260206001819286850151815501940192016102b3565b600080527f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563601f830160051c8101916020841061035f575b601f0160051c01905b81811061035357506100a4565b60008155600101610346565b909150819061033d565b90607f1690610092565b600080fdfe608080604052600436101561001357600080fd5b600090813560e01c90816301ffc9a7146110b95750806306fdde0314611009578063081812fc14610fea578063095ea7b314610e6657806318160ddd14610e485780631d6b8b7214610df657806323b872dd14610dd157806342842e0e14610d9c5780634a5dc3aa146109425780636352211e1461091157806370a082311461087b578063715018a61461081d5780638da5cb5b146107f457806395d89b4114610713578063a22cb4651461063d578063b88d4fde146105af578063b91f39a4146104ba578063c87b56dd14610283578063c98516291461022e578063e985e9c5146101d45763f2fde38b1461010857600080fd5b346101d15760203660031901126101d15761012161116e565b6101296116d0565b6001600160a01b0316801561017d57600680546001600160a01b0319811683179091556001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0600080a380f35b60405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608490fd5b80fd5b50346101d15760403660031901126101d15760406101f061116e565b916101f9611189565b9260018060a01b031681526005602052209060018060a01b0316600052602052602060ff604060002054166040519015158152f35b50346101d15760203660031901126101d157604090600435815260086020522060018060a01b0381541661027f6002830154926102726001600383015492016111fb565b936040519485948561129f565b0390f35b50346101d15760203660031901126101d1576004356000818152600260205260409020546102bb906001600160a01b031615156113ff565b6020916040516102cb84826111d9565b81815281156104a0578283839472184f03e93ff9f4daa797ed6e38ed64bf6a1f0160401b81101561047a575b5080866d04ee2d6d415b85acef8100000000600a93101561045f575b50662386f26fc1000081101561044b575b6305f5e10081101561043a575b61271081101561042b575b606481101561041d575b1015610412575b600a602160018601956103786103628861130c565b97610370604051998a6111d9565b80895261130c565b8789019690601f1901368837508601015b60001901916f181899199a1a9b1b9c1cb0b131b232b360811b8282061a835304908582156103ba5750600a90610389565b9150506103fc926103ec9460405195846103dd889651809287808a019101611126565b85019151809385840190611126565b010103601f1981018352826111d9565b905b61027f604051928284938452830190611149565b60019093019261034d565b606460029104950194610346565b6127106004910495019461033c565b6305f5e10060089104950194610331565b662386f26fc1000060109104950194610324565b906d04ee2d6d415b85acef8100000000900495019486610313565b6040955072184f03e93ff9f4daa797ed6e38ed64bf6a1f0160401b90049050600a6102f7565b5050506040516104b082826111d9565b60008152906103fe565b50346101d15760403660031901126101d1576004356104d7611189565b906104e06116d0565b6001600160a01b0382161561057157808352600260205260408320546001600160a01b03161561053557825260086020526040822080546001600160a01b0319166001600160a01b0390921691909117905580f35b60405162461bcd60e51b8152602060048201526014602482015273151bdad95b88191bd95cc81b9bdd08195e1a5cdd60621b6044820152606490fd5b60405162461bcd60e51b8152602060048201526016602482015275496e76616c69642077616c6c6574206164647265737360501b6044820152606490fd5b50346101d15760803660031901126101d1576105c961116e565b6105d1611189565b906064359060443567ffffffffffffffff83116106395736602384011215610639576106369361060e610631943690602481600401359101611328565b9261062161061c8433611471565b61139d565b61062c838383611544565b6118b9565b61177b565b80f35b8480fd5b50346101d15760403660031901126101d15761065761116e565b6024359081151580920361070f576001600160a01b0316903382146106ca5733835260056020526040832082600052602052604060002060ff1981541660ff83161790556040519081527f17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c3160203392a380f35b60405162461bcd60e51b815260206004820152601960248201527f4552433732313a20617070726f766520746f2063616c6c6572000000000000006044820152606490fd5b8280fd5b50346101d157806003193601126101d15760405160006001546107358161119f565b80845290600181169081156107d05750600114610771575b61027f8361075d818503826111d9565b604051918291602083526020830190611149565b600160009081527fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf6939250905b8082106107b65750909150810160200161075d61074d565b91926001816020925483858801015201910190929161079e565b60ff191660208086019190915291151560051b8401909101915061075d905061074d565b50346101d157806003193601126101d1576006546040516001600160a01b039091168152602090f35b50346101d157806003193601126101d1576108366116d0565b600680546001600160a01b031981169091556000906001600160a01b03167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e08280a380f35b50346101d15760203660031901126101d1576001600160a01b0361089d61116e565b1680156108ba578160409160209352600383522054604051908152f35b60405162461bcd60e51b815260206004820152602960248201527f4552433732313a2061646472657373207a65726f206973206e6f7420612076616044820152683634b21037bbb732b960b91b6064820152608490fd5b50346101d15760203660031901126101d157602061093060043561144b565b6040516001600160a01b039091168152f35b50346101d15760803660031901126101d15761095c61116e565b6024359167ffffffffffffffff83116101d157366023840112156101d15782600401359267ffffffffffffffff8411610d985760248101906024853692010111610d9857604435936109ac6116d0565b6001600160a01b038416918215610d615785151580610d55575b15610d1d57600754936000198514610d0957610631610aa4916001870160075586602098604051926109f88b856111d9565b808452600083815260026020526040902054610a20906001600160a01b031615155b15611912565b50600082815260026020526040902054610a44906001600160a01b03161515610a1a565b8760005260038a526040600020600181540190558160005260028a526040600020886001600160601b0360a01b825416179055818860007fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8180a461179f565b604051956080870187811067ffffffffffffffff821117610cf357604052838752610ad0368484611328565b868801908152604080890183815260643560608b01908152600089815260088b529290922099518a546001600160a01b0319166001600160a01b0391909116178a559151805190999290600184019067ffffffffffffffff8111610cf357610b38825461119f565b9b601f8d11610cad575b8a9b9c5060009a999a508c908d601f8411600114610bfa57509389989693836003947f389b9df65ce076b7ad850d382710b3bcacadf9799338a5ad151570ef1cd1e39c9c989460809b98600092610bef575b50508160011b9160001990871b1c19161790555b5160028401555191015560405194859360608552816060860152858501376000838301850152898301526064356040830152601f01601f19168101030190a3604051908152f35b015190503880610b94565b9190601f9b9a999b1984168560005283600020936000905b828210610c7b575050848b989460809b98947f389b9df65ce076b7ad850d382710b3bcacadf9799338a5ad151570ef1cd1e39c9e9f9b989460039860019510610c63575b505050811b019055610ba8565b015160001983891b60f8161c19169055388080610c56565b84969798999a9b9c9e9d5060018193949596829397870151815501960194018f9c9d9b9a999897969594939291610c12565b826000528b600020601f830160051c81019d8d8410610ce9575b601f0160051c019c5b8d8110610cdd5750610b42565b60008155600101610cd0565b909d508d90610cc7565b634e487b7160e01b600052604160045260246000fd5b634e487b7160e01b81526011600452602490fd5b60405162461bcd60e51b815260206004820152601060248201526f496e76616c696420736861726542707360801b6044820152606490fd5b506127108611156109c6565b60405162461bcd60e51b815260206004820152600f60248201526e496e76616c6964206164647265737360881b6044820152606490fd5b5080fd5b50346101d157610636610631610db1366112d2565b9060405192610dc16020856111d9565b86845261062161061c8433611471565b50346101d157610636610de3366112d2565b91610df161061c8433611471565b611544565b50346101d15760203660031901126101d157600435815260086020526040902080546001600160a01b031661027f610e30600184016111fb565b9260036002820154910154906040519485948561129f565b50346101d157806003193601126101d1576020600754604051908152f35b50346101d15760403660031901126101d157610e8061116e565b602435906001600160a01b03610e958361144b565b6001600160a01b039092169116818114610f9b57803314908115610f7a575b5015610f0f5781835260046020526040832080546001600160a01b031916821790556001600160a01b03610ee78361144b565b167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9258480a480f35b60405162461bcd60e51b815260206004820152603d60248201527f4552433732313a20617070726f76652063616c6c6572206973206e6f7420746f60448201527f6b656e206f776e6572206f7220617070726f76656420666f7220616c6c0000006064820152608490fd5b84525060056020908152604080852033865290915283205460ff1638610eb4565b60405162461bcd60e51b815260206004820152602160248201527f4552433732313a20617070726f76616c20746f2063757272656e74206f776e656044820152603960f91b6064820152608490fd5b50346101d15760203660031901126101d157602061093060043561135f565b50346101d157806003193601126101d157604051908080549061102b8261119f565b80855291600181169081156110925750600114611053575b61027f8461075d818603826111d9565b80805260208120939250905b8082106110785750909150810160200161075d82611043565b91926001816020925483858801015201910190929161105f565b60ff191660208087019190915292151560051b8501909201925061075d9150839050611043565b905034610d98576020366003190112610d985760043563ffffffff60e01b811680910361070f57602092506380ac58cd60e01b8114908115611115575b8115611104575b5015158152f35b6301ffc9a760e01b149050386110fd565b635b5e139f60e01b811491506110f6565b60005b8381106111395750506000910152565b8181015183820152602001611129565b9060209161116281518092818552858086019101611126565b601f01601f1916010190565b600435906001600160a01b038216820361118457565b600080fd5b602435906001600160a01b038216820361118457565b90600182811c921680156111cf575b60208310146111b957565b634e487b7160e01b600052602260045260246000fd5b91607f16916111ae565b90601f8019910116810190811067ffffffffffffffff821117610cf357604052565b906040519182600082549261120f8461119f565b808452936001811690811561127d5750600114611236575b50611234925003836111d9565b565b90506000929192526020600020906000915b8183106112615750509060206112349282010138611227565b6020919350806001915483858901015201910190918492611248565b90506020925061123494915060ff191682840152151560051b82010138611227565b6001600160a01b039091168152608060208201819052929493926060926112c891830190611149565b9460408201520152565b6060906003190112611184576004356001600160a01b038116810361118457906024356001600160a01b0381168103611184579060443590565b67ffffffffffffffff8111610cf357601f01601f191660200190565b9291926113348261130c565b9161134260405193846111d9565b829481845281830111611184578281602093846000960137010152565b600081815260026020526040902054611382906001600160a01b031615156113ff565b6000908152600460205260409020546001600160a01b031690565b156113a457565b60405162461bcd60e51b815260206004820152602d60248201527f4552433732313a2063616c6c6572206973206e6f7420746f6b656e206f776e6560448201526c1c881bdc88185c1c1c9bdd9959609a1b6064820152608490fd5b1561140657565b60405162461bcd60e51b815260206004820152601860248201527f4552433732313a20696e76616c696420746f6b656e20494400000000000000006044820152606490fd5b6000908152600260205260409020546001600160a01b031661146e8115156113ff565b90565b906001600160a01b036114838261144b565b6001600160a01b03909316921682811492919083156114c3575b5082156114a957505090565b9091506001600160a01b03906114be9061135f565b161490565b909250600052600560205260406000208160005260205260ff60406000205416913861149d565b156114f157565b60405162461bcd60e51b815260206004820152602560248201527f4552433732313a207472616e736665722066726f6d20696e636f72726563742060448201526437bbb732b960d91b6064820152608490fd5b906115656115518461144b565b6001600160a01b03938416931683146114ea565b6001600160a01b03169081158061167f578115159081611676575b50611631576115a1816001600160a01b0361159a8661144b565b16146114ea565b82600052600460205260406000206001600160601b0360a01b8154169055806000526003602052604060002060001981540190558160005260036020526040600020600181540190558260005260026020526040600020826001600160601b0360a01b8254161790557fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef600080a4565b60405162461bcd60e51b815260206004820152601f60248201527f536f756c626f756e643a207472616e73666572206e6f7420616c6c6f776564006044820152606490fd5b90501538611580565b60405162461bcd60e51b8152602060048201526024808201527f4552433732313a207472616e7366657220746f20746865207a65726f206164646044820152637265737360e01b6064820152608490fd5b6006546001600160a01b031633036116e457565b606460405162461bcd60e51b815260206004820152602060248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152fd5b60809060208152603260208201527f4552433732313a207472616e7366657220746f206e6f6e20455243373231526560408201527131b2b4bb32b91034b6b83632b6b2b73a32b960711b60608201520190565b1561178257565b60405162461bcd60e51b81528061179b60048201611728565b0390fd5b91823b156118b1576117e5926020926000604051809681958294630a85bd0160e11b84523360048501528460248501526044840152608060648401526084830190611149565b03926001600160a01b03165af1809160009161186e575b5090611858573d15611851573d6118128161130c565b9061182060405192836111d9565b81523d6000602083013e5b8051908161184c5760405162461bcd60e51b81528061179b60048201611728565b602001fd5b606061182b565b6001600160e01b031916630a85bd0160e11b1490565b6020813d6020116118a9575b81611887602093836111d9565b81010312610d985751906001600160e01b0319821682036101d15750386117fc565b3d915061187a565b505050600190565b919290803b15611909576117e593600060209460405196879586948593630a85bd0160e11b855233600486015260018060a01b031660248501526044840152608060648401526084830190611149565b50505050600190565b1561191957565b60405162461bcd60e51b815260206004820152601c60248201527f4552433732313a20746f6b656e20616c7265616479206d696e746564000000006044820152606490fdfea2646970667358221220b67c505017622797725b6de67ed72df4fa9a315b84d817a8c3d85803337a751164736f6c634300081c0033";

    private static String librariesLinkedBinary;

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_GETAPPROVED = "getApproved";

    public static final String FUNC_GETSTAKEHOLDER = "getStakeholder";

    public static final String FUNC_ISAPPROVEDFORALL = "isApprovedForAll";

    public static final String FUNC_MINT = "mint";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_OWNEROF = "ownerOf";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_safeTransferFrom = "safeTransferFrom";

    public static final String FUNC_SETAPPROVALFORALL = "setApprovalForAll";

    public static final String FUNC_STAKEHOLDERS = "stakeholders";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOKENURI = "tokenURI";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_UPDATEWALLET = "updateWallet";

    public static final Event APPROVAL_EVENT = new Event("Approval",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }));;

    public static final Event APPROVALFORALL_EVENT = new Event("ApprovalForAll",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Bool>() {
        }));;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }));;

    public static final Event STAKEHOLDERMINTED_EVENT = new Event("StakeholderMinted",
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Utf8String>() {
        }, new TypeReference<Uint256>() {
        }, new TypeReference<Uint256>() {
        }));;

    public static final Event TRANSFER_EVENT = new Event("Transfer",
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {
        }, new TypeReference<Address>(true) {
        }, new TypeReference<Uint256>(true) {
        }));;

    @Deprecated
    protected StakeholderNFT(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected StakeholderNFT(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected StakeholderNFT(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected StakeholderNFT(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVAL_EVENT,
            transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApprovalEventResponse getApprovalEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalEventFromLog(log));
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

    public static List<ApprovalForAllEventResponse> getApprovalForAllEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVALFORALL_EVENT,
            transactionReceipt);
        ArrayList<ApprovalForAllEventResponse> responses = new ArrayList<ApprovalForAllEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static ApprovalForAllEventResponse getApprovalForAllEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVALFORALL_EVENT, log);
        ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalForAllEventFromLog(log));
    }

    public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVALFORALL_EVENT));
        return approvalForAllEventFlowable(filter);
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

    public static List<StakeholderMintedEventResponse> getStakeholderMintedEvents(
        TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(STAKEHOLDERMINTED_EVENT,
            transactionReceipt);
        ArrayList<StakeholderMintedEventResponse> responses = new ArrayList<StakeholderMintedEventResponse>(
            valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            StakeholderMintedEventResponse typedResponse = new StakeholderMintedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.wallet = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.role = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.shareBps = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.eventNftId = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static StakeholderMintedEventResponse getStakeholderMintedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(STAKEHOLDERMINTED_EVENT, log);
        StakeholderMintedEventResponse typedResponse = new StakeholderMintedEventResponse();
        typedResponse.log = log;
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.wallet = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.role = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.shareBps = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.eventNftId = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<StakeholderMintedEventResponse> stakeholderMintedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getStakeholderMintedEventFromLog(log));
    }

    public Flowable<StakeholderMintedEventResponse> stakeholderMintedEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STAKEHOLDERMINTED_EVENT));
        return stakeholderMintedEventFlowable(filter);
    }

    public static List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT,
            transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock,
        DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String to, BigInteger tokenId) {
        final Function function = new Function(FUNC_APPROVE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String owner) {
        final Function function = new Function(FUNC_BALANCEOF,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> getApproved(BigInteger tokenId) {
        final Function function = new Function(FUNC_GETAPPROVED,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple4<String, String, BigInteger, BigInteger>> getStakeholder(BigInteger tokenId) {
        final Function function = new Function(FUNC_GETSTAKEHOLDER,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Utf8String>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }));
        return new RemoteFunctionCall<Tuple4<String, String, BigInteger, BigInteger>>(function,
            new Callable<Tuple4<String, String, BigInteger, BigInteger>>() {
                @Override
                public Tuple4<String, String, BigInteger, BigInteger> call() throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple4<String, String, BigInteger, BigInteger>((String) results.get(0).getValue(),
                        (String) results.get(1).getValue(), (BigInteger) results.get(2).getValue(),
                        (BigInteger) results.get(3).getValue());
                }
            });
    }

    public RemoteFunctionCall<Boolean> isApprovedForAll(String owner, String operator) {
        final Function function = new Function(FUNC_ISAPPROVEDFORALL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner),
                new org.web3j.abi.datatypes.Address(160, operator)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> mint(String to, String role, BigInteger shareBps,
        BigInteger eventNftId) {
        final Function function = new Function(FUNC_MINT,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to),
                new org.web3j.abi.datatypes.Utf8String(role), new org.web3j.abi.datatypes.generated.Uint256(shareBps),
                new org.web3j.abi.datatypes.generated.Uint256(eventNftId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> ownerOf(BigInteger tokenId) {
        final Function function = new Function(FUNC_OWNEROF,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(FUNC_RENOUNCEOWNERSHIP, Arrays.<Type>asList(),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger tokenId) {
        final Function function = new Function(FUNC_safeTransferFrom,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to), new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger tokenId,
        byte[] data) {
        final Function function = new Function(FUNC_safeTransferFrom,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to), new org.web3j.abi.datatypes.generated.Uint256(tokenId),
                new org.web3j.abi.datatypes.DynamicBytes(data)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setApprovalForAll(String operator, Boolean approved) {
        final Function function = new Function(FUNC_SETAPPROVALFORALL,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, operator),
                new org.web3j.abi.datatypes.Bool(approved)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple4<String, String, BigInteger, BigInteger>> stakeholders(BigInteger param0) {
        final Function function = new Function(FUNC_STAKEHOLDERS,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
            }, new TypeReference<Utf8String>() {
            }, new TypeReference<Uint256>() {
            }, new TypeReference<Uint256>() {
            }));
        return new RemoteFunctionCall<Tuple4<String, String, BigInteger, BigInteger>>(function,
            new Callable<Tuple4<String, String, BigInteger, BigInteger>>() {
                @Override
                public Tuple4<String, String, BigInteger, BigInteger> call() throws Exception {
                    List<Type> results = executeCallMultipleValueReturn(function);
                    return new Tuple4<String, String, BigInteger, BigInteger>((String) results.get(0).getValue(),
                        (String) results.get(1).getValue(), (BigInteger) results.get(2).getValue(),
                        (BigInteger) results.get(3).getValue());
                }
            });
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final Function function = new Function(FUNC_SUPPORTSINTERFACE,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
            }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> tokenURI(BigInteger tokenId) {
        final Function function = new Function(FUNC_TOKENURI,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
            }));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
            }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to, BigInteger tokenId) {
        final Function function = new Function(FUNC_TRANSFERFROM,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
                new org.web3j.abi.datatypes.Address(160, to), new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateWallet(BigInteger tokenId, String newWallet) {
        final Function function = new Function(FUNC_UPDATEWALLET,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId),
                new org.web3j.abi.datatypes.Address(160, newWallet)),
            Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static StakeholderNFT load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return new StakeholderNFT(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static StakeholderNFT load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return new StakeholderNFT(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static StakeholderNFT load(String contractAddress, Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return new StakeholderNFT(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static StakeholderNFT load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return new StakeholderNFT(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<StakeholderNFT> deploy(Web3j web3j, Credentials credentials,
        ContractGasProvider contractGasProvider) {
        return deployRemoteCall(StakeholderNFT.class, web3j, credentials, contractGasProvider, getDeploymentBinary(),
            "");
    }

    public static RemoteCall<StakeholderNFT> deploy(Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider contractGasProvider) {
        return deployRemoteCall(StakeholderNFT.class, web3j, transactionManager, contractGasProvider,
            getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<StakeholderNFT> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice,
        BigInteger gasLimit) {
        return deployRemoteCall(StakeholderNFT.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(),
            "");
    }

    @Deprecated
    public static RemoteCall<StakeholderNFT> deploy(Web3j web3j, TransactionManager transactionManager,
        BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(StakeholderNFT.class, web3j, transactionManager, gasPrice, gasLimit,
            getDeploymentBinary(), "");
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

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String approved;

        public BigInteger tokenId;
    }

    public static class ApprovalForAllEventResponse extends BaseEventResponse {
        public String owner;

        public String operator;

        public Boolean approved;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class StakeholderMintedEventResponse extends BaseEventResponse {
        public BigInteger tokenId;

        public String wallet;

        public String role;

        public BigInteger shareBps;

        public BigInteger eventNftId;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger tokenId;
    }
}
